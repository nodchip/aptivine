package aptivine;

import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

/**
 * パッケージを表すクラス
 */
public class Package {

  private String id;
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
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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
    return MoreObjects.toStringHelper(this).add("id", id).add("fileName", fileName).add("url", url)
        .add("fileSize", fileSize).add("comment", comment).add("original", original)
        .add("downloadCount", downloadCount).add("datetime", datetime).toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Package)) {
      return false;
    }
    Package rh = (Package) obj;
    return Objects.equal(id, rh.id) && Objects.equal(fileName, rh.fileName)
        && Objects.equal(url, rh.url) && Objects.equal(fileSize, rh.fileSize)
        && Objects.equal(comment, rh.comment) && Objects.equal(original, rh.original)
        && downloadCount == rh.downloadCount && Objects.equal(datetime, rh.datetime);
  }
}
