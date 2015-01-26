package aptivine;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageUtils {

  private static final Logger logger = Logger.getLogger(Package.class.toString());

  private static final Pattern NAME_PATTERN1 = Pattern.compile("^(.+?)[._-]?([0-9.]+)$");

  public String getId(String fileName) {
    String title = normalizeTitle(getFileTitle(fileName));

    Matcher matcher = NAME_PATTERN1.matcher(title);
    if (!matcher.find()) {
      return title;
    }

    return matcher.group(1);
  }

  public double getVersionAsDouble(String fileName) {
    return toDouble(getVersionAsString(fileName));
  }

  public double toDouble(String version) {
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
      logger.log(Level.WARNING, "バージョン番号の書式が不正です: version=" + version, e);
      return 0;
    }
  }

  public String getVersionAsString(String fileName) {
    String title = normalizeTitle(getFileTitle(fileName));
    Matcher matcher = NAME_PATTERN1.matcher(title);
    if (!matcher.find()) {
      return null;
    }

    return matcher.group(2);
  }

  private String getFileTitle(String fileName) {
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
