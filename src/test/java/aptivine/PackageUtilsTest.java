package aptivine;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PackageUtilsTest {

  private PackageUtils packageUtils;

  @Before
  public void setUp() throws Exception {
    packageUtils = new PackageUtils();
  }

  @Test
  public void getIdReturnsId1() {
    assertThat(packageUtils.getId("youtube&#45;2.03.zip")).isEqualTo("youtube");
  }

  @Test
  public void getVersionReturnsVerson1() {
    assertThat(packageUtils.getVersionAsDouble("youtube&#45;2.03.zip")).isEqualTo(2.03);
  }

  @Test
  public void getIdReturnsId2() {
    assertThat(packageUtils.getId("uploadrocket&#45;0.1_1.zip")).isEqualTo("uploadrocket");
  }

  @Test
  public void getVersionReturnsVerson2() {
    assertThat(packageUtils.getVersionAsDouble("uploadrocket&#45;0.1_1.zip")).isEqualTo(0.11);
  }

  @Test
  public void getIdReturnsId3() {
    assertThat(packageUtils.getId("dorothy2_middling_140714.zip")).isEqualTo("dorothy2_middling");
  }

  @Test
  public void getVersionReturnsVerson3() {
    assertThat(packageUtils.getVersionAsDouble("dorothy2_middling_140714.zip")).isEqualTo(140714.0);
  }

  @Test
  public void getIdReturnsId4() {
    assertThat(packageUtils.getId("DOA.0.26.zip")).isEqualTo("DOA");
  }

  @Test
  public void getVersionReturnsVerson4() {
    assertThat(packageUtils.getVersionAsDouble("DOA.0.26.zip")).isEqualTo(0.26);
  }

  @Test
  public void getIdReturnsId5() {
    assertThat(packageUtils.getId("instagram.27.140.201112162000.rar")).isEqualTo("instagram");
  }

  @Test
  public void getVersionReturnsVerson5() {
    assertThat(packageUtils.getVersionAsDouble("instagram.27.140.201112162000.rar")).isEqualTo(
        27.140201112162000);
  }

  @Test
  public void getIdReturnsId6() {
    assertThat(packageUtils.getId("xor130224.zip")).isEqualTo("xor");
  }

  @Test
  public void getVersionReturnsVerson6() {
    assertThat(packageUtils.getVersionAsDouble("xor130224.zip")).isEqualTo(130224.0);
  }

  @Test
  public void getIdReturnsId7() {
    assertThat(packageUtils.getId("DOA018.zip")).isEqualTo("DOA");
  }

  @Test
  public void getVersionReturnsVerson7() {
    assertThat(packageUtils.getVersionAsDouble("DOA018.zip")).isEqualTo(0.18);
  }
}
