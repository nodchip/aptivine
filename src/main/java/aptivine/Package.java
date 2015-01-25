package aptivine;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

/**
 * パッケージを表すクラス
 * 
 * ID が fileName から自動生成される点に注意。 データベースのことを考えると使いにくいが、設計間違えたので仕方がない。
 */
public class Package {

  private static final Logger logger = Logger.getLogger(Package.class.toString());

  private static final Pattern NAME_PATTERN1 = Pattern.compile("^(.+?)[._-]?([0-9.]+)$");

  private String fileName;
  private String url;
  private String fileSize;
  private String comment;
  private String original;
  private int downloadCount;
  private String datetime;

  // ///////////////////////////////////////////////////////////////////////////
  // Beans用メソッド
  // ///////////////////////////////////////////////////////////////////////////
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFileSize() {
    return fileSize;
  }

  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getOriginal() {
    return original;
  }

  public void setOriginal(String original) {
    this.original = original;
  }

  public int getDownloadCount() {
    return downloadCount;
  }

  public void setDownloadCount(int downloadCount) {
    this.downloadCount = downloadCount;
  }

  public String getDatetime() {
    return datetime;
  }

  public void setDatetime(String datetime) {
    this.datetime = datetime;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // その他のメソッド
  // ///////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("fileName", fileName).add("url", url)
        .add("fileSize", fileSize).add("comment", comment).add("original", original)
        .add("downloadCount", downloadCount).add("datetime", datetime).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Package)) {
      return false;
    }
    Package rh = (Package) obj;
    return Objects.equal(fileName, rh.fileName) && Objects.equal(url, rh.url)
        && Objects.equal(fileSize, rh.fileSize) && Objects.equal(comment, rh.comment)
        && Objects.equal(original, rh.original) && downloadCount == rh.downloadCount
        && Objects.equal(datetime, rh.datetime);
  }

  /**
   * IDを取得する
   * 
   * @return ID
   */
  public String getId() {
    String title = normalizeTitle(getFileTitle());

    Matcher matcher = NAME_PATTERN1.matcher(title);
    if (!matcher.find()) {
      return title;
    }

    return matcher.group(1);
  }

  /**
   * 何もしない
   * 
   * Beansの要件を満たすためのダミー実装
   */
  public void setId(String id) {
    // 何もしない
  }

  public double getVersionAsDouble() {
    String version = getVersionAsString();
    if (version == null) {
      return 0.0;
    }

    // 2個目以降の記号を空白に変える
    String[] split = version.split("\\W", 2);
    if (split.length == 1) {
      // DOA018.zip -> 0.18
      if (version.startsWith("0")) {
        version = version.substring(0, 1) + "." + version.substring(1);
      }
    } else if (split.length == 2) {
      version = split[0] + "." + split[1].replaceAll("\\D", "");
    }

    try {
      return Double.valueOf(version);
    } catch (NumberFormatException e) {
      logger.log(Level.WARNING,
          String.format("バージョン番号の書式が不正です: version=%s", fileName, getVersionAsString()), e);
      return 0;
    }
  }

  public String getVersionAsString() {
    String title = normalizeTitle(getFileTitle());
    Matcher matcher = NAME_PATTERN1.matcher(title);
    if (!matcher.find()) {
      return null;
    }

    return matcher.group(2);
  }

  private String getFileTitle() {
    String title = fileName;
    if (title.contains(".")) {
      int index = title.lastIndexOf('.');
      title = title.substring(0, index);
    }
    return title;
  }

  private String normalizeTitle(String title) {
    // "&#45;"　を "-" に変換する
    title = title.replaceAll("&#45;", "-");

    // "_1" を "1" に変換する
    for (int suffix = 1; suffix < 10; ++suffix) {
      title = title.replaceAll("_" + suffix + "$", "" + suffix);
    }
    return title;
  }
}
