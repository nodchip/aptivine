package aptivine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

import aptivine.database.Database;
import aptivine.database.DatabaseException;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.Inject;

public class Controller {

  private static final Logger logger = Logger.getLogger(Controller.class.toString());

  private static final String INDEX_URL_FORMAT = "http://u1.getuploader.com/irvn/index/%d/date/desc";
  private static final Pattern ROW_PATTERN = Pattern
      .compile("<tr.*?><td><a href=\"(.+?)\" title=\".+?\">(.+?)</a></td><td>(.+?)</td><td>(.+?)</td><td>(.+?)</td><td>(.+?)</td><td>(.+?)</td><td><a class=\"button\" href=\".+?\" title=\"編集\">Edit</a></td></tr>");
  private static final Pattern TOKEN_PATTERN = Pattern
      .compile("<input type=\"hidden\" name=\"token\" value=\"(.+?)\" />");
  private static final Pattern DOWNLOAD_LINK_PATTERN = Pattern
      .compile("<a href=\"(.+?)\" title=\".+? を ダウンロード\">");
  private static final String KEY_IRVINE_FOLDER_PATH = "irvineFolderPath";

  private final View view;
  private final Database database;
  private final PackageUtils packageUtils;
  private final Downloader downloader;
  private Map<String, Package> packages;

  @Inject
  public Controller(View view, Database database, PackageUtils packageUtils, Downloader downloader) {
    this.view = checkNotNull(view);
    this.database = checkNotNull(database);
    this.packageUtils = checkNotNull(packageUtils);
    this.downloader = checkNotNull(downloader);
  }

  public void start() {
    view.setController(this);

    new Thread(() -> {
      try {
        initialize();
      } catch (Exception e) {
        logger.log(Level.SEVERE, "初期化に失敗しました", e);
        throw Throwables.propagate(e);
      }
    }).start();

    view.start();
  }

  private void initialize() throws DatabaseException {
    // Irvineフォルダの読み込み
    String irvineFolderPath = database.loadSetting(KEY_IRVINE_FOLDER_PATH);
    if (!Strings.isNullOrEmpty(irvineFolderPath)) {
      view.setIrvineFolderPath(irvineFolderPath);
    }

    // パッケージ情報の取得
    reload();
  }

  public void reload() {
    view.setEnabled(false);

    try {
      doReload();
    } catch (IOException e) {
      view.setStatusBar("パッケージ情報の取得に失敗しました: " + e.getMessage());
      logger.log(Level.WARNING, "パッケージ情報の取得に失敗しました", e);
    }

    view.setEnabled(true);
  }

  private void doReload() throws IOException {
    List<Package> packages = getUploadedFiles();

    Map<String, Package> uniqued = new TreeMap<>();
    for (Package file : packages) {
      String id = file.getId();
      if (uniqued.containsKey(id)) {
        Package existingPackage = uniqued.get(id);
        double existingVersion = packageUtils.getVersionAsDouble(existingPackage.getFileName());
        double newVersion = packageUtils.getVersionAsDouble(file.getFileName());
        if (existingVersion > newVersion) {
          continue;
        }
      }
      uniqued.put(id, file);
    }
    this.packages = uniqued;

    Map<String, Package> installedPackages;
    try {
      installedPackages = database.loadInstalledPackages();
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "インストール済みパッケージの読み込みに失敗しました", e);
      view.setStatusBar("インストール済みパッケージの読み込みに失敗しました");
      view.setEnabled(true);
      return;
    }

    view.setUploadedFiles(new ArrayList<>(uniqued.values()), installedPackages);
  }

  private List<Package> getUploadedFiles() throws IOException {
    List<Package> files = new ArrayList<>();
    for (int page = 1;; ++page) {
      view.setStatusBar(String.format("パッケージ情報を取得中です (%d)", page));

      List<Package> filesInPage = getUploadedFiles(page);
      if (filesInPage.isEmpty()) {
        break;
      }
      files.addAll(filesInPage);
    }

    view.setStatusBar("");

    return files;
  }

  private List<Package> getUploadedFiles(int page) throws IOException {
    String url = String.format(INDEX_URL_FORMAT, page);
    String html = downloader.downloadAsString(url);
    Matcher matcher = ROW_PATTERN.matcher(html);
    List<Package> files = new ArrayList<Package>();
    while (matcher.find()) {
      Package file = new Package();
      try {
        new URL(matcher.group(1));
      } catch (MalformedURLException e) {
        logger.log(Level.WARNING, "URLの書式が不正です: url=" + matcher.group(1), e);
        continue;
      }
      file.setUrl(matcher.group(1));
      file.setId(packageUtils.getId(matcher.group(2)));
      file.setFileName(matcher.group(2));
      file.setFileSize(matcher.group(3));
      file.setComment(matcher.group(4));
      file.setOriginal(matcher.group(5));
      try {
        file.setDownloadCount(Integer.valueOf(matcher.group(6)));
      } catch (NumberFormatException e) {
        logger.log(Level.WARNING, "ダウンロードカウントの書式が不正です: downloadCount=" + matcher.group(6));
      }
      file.setDatetime(matcher.group(7));
      files.add(file);
    }

    return files;
  }
  
  public void onIrvineFolderPathSelected(String irvineFolderPath){
    try {
      database.saveSetting(KEY_IRVINE_FOLDER_PATH, irvineFolderPath);
    } catch (DatabaseException e) {
      logger.log(Level.WARNING, "Irvineフォルダパスの保存に失敗しました",e); 
    }
  }

  public void markAllUpgrade() {
    view.markAllUpgrade();
  }

  public void apply(String irvineFolderPath, List<String> markedPackageIds) {
    view.setEnabled(false);

    try {
      doApply(irvineFolderPath, markedPackageIds);
      doReload();
    } catch (IOException | ArchiveException | DatabaseException e) {
      view.setStatusBar("パッケージの更新に失敗しました: " + e.getMessage());
      logger.log(Level.WARNING, "パッケージの更新に失敗しました", e);
    }

    view.setEnabled(true);
  }

  public void doApply(String irvineFolderPath, List<String> markedPackageIds) throws IOException,
      ArchiveException, DatabaseException {
    File outputDirectory = new File(irvineFolderPath);

    int counter = 0;
    for (String id : markedPackageIds) {
      view.setProgress(0, markedPackageIds.size(), counter++);

      // ファイルページの取得
      Package p = packages.get(id);
      String filePageUrl = p.getUrl();
      view.setStatusBar("ファイルページを取得しています: filePageUrl=" + filePageUrl);
      String filePageHtml = downloader.downloadAsString(filePageUrl);
      String token = extractToken(filePageHtml);
      if (token == null) {
        throw new IOException("ダウンロードトークンが見つかりませんでした: filePageUrl=" + filePageUrl);
      }

      // ダウンロードページの取得
      view.setStatusBar("ダウンロードページを取得しています: url=" + filePageUrl);
      String downloadPagehtml = downloader.downloadAsString(filePageUrl, "token", token);

      // パッケージの取得
      String packageUrl = extractDownloadLink(downloadPagehtml);
      if (packageUrl == null) {
        throw new IOException("ダウンロードリンクが見つかりませんでした: filePageUrl=" + filePageUrl);
      }
      view.setStatusBar("パッケージを取得しています: packageUrl=" + packageUrl);
      File file = File.createTempFile("aptivine", null);
      downloader.downloadAsFile(packageUrl, file);

      // パッケージの展開
      view.setStatusBar("パッケージの展開中です: id=" + id);
      try (BufferedInputStream packageInputStream = new BufferedInputStream(new FileInputStream(
          file));
          ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
              .createArchiveInputStream(packageInputStream)) {
        ArchiveEntry archiveEntry;
        while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
          if (archiveEntry.isDirectory()) {
            continue;
          }

          // ファイルの展開
          String name = archiveEntry.getName();
          if (!name.contains("Dorothy2")) {
            continue;
          }
          name = name.substring(name.indexOf("Dorothy2"));
          view.setStatusBar("パッケージの展開中です: name=" + name);
          File outputFile = new File(outputDirectory, name);
          Files.createParentDirs(outputFile);
          try (BufferedOutputStream fileOutputStream = new BufferedOutputStream(
              new FileOutputStream(outputFile))) {
            ByteStreams.copy(archiveInputStream, fileOutputStream);
          }
        }
      }

      // データベースの更新
      database.saveInstalledPackage(p);
    }

    view.setStatusBar("アップデートが完了しました");
    view.setProgress(0, markedPackageIds.size(), markedPackageIds.size());
  }

  @Nullable
  private String extractToken(String html) {
    Matcher matcher = TOKEN_PATTERN.matcher(html);
    if (!matcher.find()) {
      return null;
    }
    return matcher.group(1);
  }

  @Nullable
  private String extractDownloadLink(String html) {
    Matcher matcher = DOWNLOAD_LINK_PATTERN.matcher(html);
    if (!matcher.find()) {
      return null;
    }
    return matcher.group(1);
  }
}
