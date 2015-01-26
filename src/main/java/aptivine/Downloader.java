package aptivine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.GenericData;
import com.google.common.base.Charsets;
import com.google.inject.Inject;

public class Downloader {

  private static final Logger logger = Logger.getLogger(Downloader.class.toString());

  private final HttpTransport httpTransport;

  @Inject
  public Downloader(HttpTransport httpTransport) {
    this.httpTransport = checkNotNull(httpTransport);
  }

  public String downloadAsString(String url, String... params) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    int statusCode = download(url, outputStream, params);
    if (statusCode / 100 != 2) {
      return null;
    }
    return new String(outputStream.toByteArray(), Charsets.UTF_8);
  }

  public int downloadAsFile(String url, File outputFile) throws IOException {
    try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(
        outputFile))) {
      return download(url, outputStream);
    }
  }

  /**
   * 指定された URL からデータをダウンロードし、出力ストリームに書き出す。
   * 
   * @param url
   *          URL
   * @param outputStream
   *          出力ストリーム
   * @param params
   *          POSTパラメーターに渡すパラメーター
   * @return ステータスコード
   * @throws IOException
   *           エラー発生時
   */
  public int download(String url, OutputStream outputStream, String... params) throws IOException {
    checkArgument(params.length % 2 == 0);
    logger.log(Level.FINE,
        String.format("downloadAsString(): url=%s params=%s", url, Arrays.deepToString(params)));

    HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
    GenericUrl genericUrl = new GenericUrl(url);

    HttpRequest request;
    if (params.length == 0) {
      request = requestFactory.buildGetRequest(genericUrl);
    } else {
      GenericData genericData = new GenericData();
      for (int index = 0; index < params.length; index += 2) {
        genericData.put(params[index], params[index + 1]);
      }
      UrlEncodedContent httpContent = new UrlEncodedContent(genericData);
      request = requestFactory.buildPostRequest(genericUrl, httpContent);
    }

    HttpResponse response = request.execute();
    try {
      if (response.getStatusCode() / 100 != 2) {
        throw new IOException(String.format("ダウンロードに失敗しました: url=%s statusCode=%d", url,
            response.getStatusCode()));
      }
      response.download(outputStream);
      return response.getStatusCode();
    } finally {
      response.disconnect();
    }
  }
}
