package aptivine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import aptivine.database.Database;
import aptivine.database.DatabaseException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Charsets;
import com.google.inject.Inject;

public class Controller {

  private static final Logger logger = Logger.getLogger(Controller.class.toString());

  private static final String INDEX_URL_FORMAT = "http://u1.getuploader.com/irvn/index/%d/date/desc";
  private static final Pattern ROW_PATTERN = Pattern
      .compile("<tr.+?><td><a href=\"(.+?)\" title=\".+?\">(.+?)</a></td><td>(.+?)</td><td>(.+?)</td><td>(.+?)</td><td>(.+?)</td><td>(.+?)</td><td><a class=\"button\" href=\".+?\" title=\"編集\">Edit</a></td></tr>");

  private final HttpTransport httpTransport;
  private final View view;
  private final Database database;
  private final PackageUtils packageUtils;
  private Map<String, Package> packages;

  @Inject
  public Controller(HttpTransport httpTransport, View view, Database database,
      PackageUtils packageUtils) {
    this.httpTransport = checkNotNull(httpTransport);
    this.view = checkNotNull(view);
    this.database = checkNotNull(database);
    this.packageUtils = checkNotNull(packageUtils);
  }

  public void start() {
    view.setController(this);

    new Thread(() -> {
      reload();
    }).start();

    view.start();
  }

  public void reload() {
    view.setEnabled(false);

    List<Package> packages = getUploadedFiles();

    Map<String, Package> uniqued = new TreeMap<>();
    for (Package file : packages) {
      if (uniqued.containsKey(file.getId())) {
        Package existingPackage = uniqued.get(file.getId());
        double existingVersion = packageUtils.getVersionAsDouble(existingPackage.getFileName());
        double newVersion = packageUtils.getVersionAsDouble(file.getFileName());
        if (existingVersion > newVersion) {
          continue;
        }
      }
      uniqued.put(file.getId(), file);
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

    view.setEnabled(true);
  }

  private List<Package> getUploadedFiles() {
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

  private List<Package> getUploadedFiles(int page) {
    String url = String.format(INDEX_URL_FORMAT, page);
    String html = downloadAsString(url);
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

  private String downloadAsString(String url) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    int statusCode = download(url, outputStream);
    logger.log(Level.INFO,
        String.format("downloadAsString(): url=%s statusCode=%d", url, statusCode));

    if (statusCode / 100 != 2) {
      return null;
    }

    return new String(outputStream.toByteArray(), Charsets.UTF_8);
  }

  private int download(String url, OutputStream outputStream) {
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    GenericUrl genericUrl = new GenericUrl(url);
    HttpRequest request;
    try {
      request = requestFactory.buildGetRequest(genericUrl);
    } catch (IOException e) {
      logger.log(Level.WARNING, "ダウンロードに失敗しました: url=" + url, e);
      return 0;
    }

    HttpResponse response;
    try {
      response = request.execute();
    } catch (IOException e) {
      logger.log(Level.WARNING, "ダウンロードに失敗しました: url=" + url, e);
      return 0;
    }

    if (response.getStatusCode() / 100 != 2) {
      logger.log(Level.WARNING,
          String.format("ダウンロードに失敗しました: url=%s statusCode=%d", url, response.getStatusCode()));
      return response.getStatusCode();
    }

    try {
      response.download(outputStream);
    } catch (IOException e) {
      logger.log(Level.WARNING, "ダウンロードに失敗しました: url=" + url, e);
      return 0;
    }

    return response.getStatusCode();
  }

  public void markAllUpgrade() {

  }

  public void apply() {

  }
}
