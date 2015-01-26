package aptivine.database;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanMapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import aptivine.Package;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class Database {

  private static final String ID = "id";

  private final QueryRunner queryRunner;

  @Inject
  public Database(QueryRunner queryRunner) throws SQLException {
    this.queryRunner = Preconditions.checkNotNull(queryRunner);

    initialize();
  }

  private void initialize() throws SQLException {
    queryRunner.update("CREATE TABLE IF NOT EXISTS installed_package (" //
        + "  id TEXT PRIMARY KEY," //
        + "  fileName TEXT," //
        + "  url TEXT," //
        + "  fileSize TEXT," //
        + "  comment TEXT," //
        + "  original TEXT," //
        + "  downloadCount INTEGER," //
        + "  datetime TEXT" //
        + ");");
    queryRunner.update("CREATE TABLE IF NOT EXISTS setting (" //
        + "  key TEXT PRIMARY KEY," //
        + "  value TEXT" //
        + ");");
  }

  public Map<String, Package> loadInstalledPackages() throws DatabaseException {
    try {
      return queryRunner.query("SELECT * FROM installed_package", new BeanMapHandler<>(
          Package.class, ID));
    } catch (SQLException e) {
      throw new DatabaseException("インストール済みパッケージの読み込みに失敗しました", e);
    }
  }

  public void saveInstalledPackage(Package p) throws DatabaseException {
    try {
      queryRunner.update("REPLACE INTO installed_package "
          + "(id, fileName, url, fileSize, comment, original, downloadCount, datetime) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", p.getId(), p.getFileName(), p.getUrl(),
          p.getFileSize(), p.getComment(), p.getOriginal(), p.getDownloadCount(), p.getDatetime());
    } catch (SQLException e) {
      throw new DatabaseException("インストール済みパッケージの保存に失敗しました", e);
    }
  }

  public String loadSetting(String key) throws DatabaseException {
    try {
      return queryRunner.query("SELECT value FROM setting", new ScalarHandler<String>());
    } catch (SQLException e) {
      throw new DatabaseException("設定の読み込みに失敗しました", e);
    }
  }

  public void saveSetting(String key, String value) throws DatabaseException {
    try {
      queryRunner.update("REPLACE INTO setting (key, value) VALUES (?, ?)", key, value);
    } catch (SQLException e) {
      throw new DatabaseException("設定の保存に失敗しました", e);
    }
  }
}
