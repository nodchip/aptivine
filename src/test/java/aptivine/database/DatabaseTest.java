package aptivine.database;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import aptivine.Package;

import com.google.common.collect.ImmutableMap;
import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(JUnit4.class)
public class DatabaseTest {

  public static class DatabaseTestEnv extends AbstractModule {
    @Override
    protected void configure() {
      install(new GuiceBerryModule());
      install(new DatabaseModule("test.db"));
    }
  }

  @Rule
  public final GuiceBerryRule rule = new GuiceBerryRule(DatabaseTestEnv.class);
  @Inject
  private Database database;
  @Inject
  QueryRunner queryRunner;

  @Before
  public void setUp() throws Exception {
    queryRunner.update("DELETE FROM installed_package");
  }

  @Test
  public void loadInstalledPackagesReturnsInstalledPackages() throws Exception {
    queryRunner.update("INSERT INTO installed_package "
        + "(id, fileName, url, fileSize, comment, original, downloadCount, datetime) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", "id", "fileName", "url", "fileSize", "comment",
        "original", 111, "datetime");

    Map<String, Package> actual = database.loadInstalledPackages();

    Package expectedValue = new Package();
    // expectedValue.setId("id");
    expectedValue.setFileName("fileName");
    expectedValue.setUrl("url");
    expectedValue.setFileSize("fileSize");
    expectedValue.setComment("comment");
    expectedValue.setOriginal("original");
    expectedValue.setDownloadCount(111);
    expectedValue.setDatetime("datetime");
    assertThat(actual).isEqualTo(ImmutableMap.of("id", expectedValue));
  }

  @Test
  public void saveInstalledPackageStoresInstalledPackage() throws Exception {
    Package p = new Package();
    // expectedValue.setId("id");
    p.setFileName("fileName");
    p.setUrl("url");
    p.setFileSize("fileSize");
    p.setComment("comment");
    p.setOriginal("original");
    p.setDownloadCount(111);
    p.setDatetime("datetime");

    database.saveInstalledPackage(p);

    assertThat(database.loadInstalledPackages()).isEqualTo(ImmutableMap.of(p.getId(), p));
  }
}
