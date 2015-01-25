package aptivine.database;

import static com.google.common.base.Preconditions.checkArgument;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import com.google.api.client.util.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

public class DatabaseModule extends AbstractModule {

  private static final String DRIVER_CLASS_NAME = "org.sqlite.JDBC";
  private static final String VALIDATION_QUERY = "SELECT 1";
  private final String url;

  public DatabaseModule(String fileName) {
    checkArgument(!Strings.isNullOrEmpty(fileName));
    this.url = "jdbc:sqlite:" + fileName;
  }

  @Override
  protected void configure() {
    bind(Database.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  private DataSource provideDataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setDriverClassName(DRIVER_CLASS_NAME);
    dataSource.setUrl(url);
    dataSource.setValidationQuery(VALIDATION_QUERY);
    return dataSource;
  }

  @Provides
  @Singleton
  private QueryRunner provideQueryRunner(DataSource dataSource) {
    return new QueryRunner(dataSource);
  }
}
