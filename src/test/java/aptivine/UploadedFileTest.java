package aptivine;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

public class UploadedFileTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void getIdReturnsId1() {
    Package file = new Package();
    file.setFileName("youtube&#45;2.03.zip");

    assertThat(file.getId()).isEqualTo("youtube");
  }

  @Test
  public void getVersionReturnsVerson1() {
    Package file = new Package();
    file.setFileName("youtube&#45;2.03.zip");

    assertThat(file.getVersionAsDouble()).isEqualTo(2.03);
  }

  @Test
  public void getIdReturnsId2() {
    Package file = new Package();
    file.setFileName("uploadrocket&#45;0.1_1.zip");

    assertThat(file.getId()).isEqualTo("uploadrocket");
  }

  @Test
  public void getVersionReturnsVerson2() {
    Package file = new Package();
    file.setFileName("uploadrocket&#45;0.1_1.zip");

    assertThat(file.getVersionAsDouble()).isEqualTo(0.11);
  }

  @Test
  public void getIdReturnsId3() {
    Package file = new Package();
    file.setFileName("dorothy2_middling_140714.zip");

    assertThat(file.getId()).isEqualTo("dorothy2_middling");
  }

  @Test
  public void getVersionReturnsVerson3() {
    Package file = new Package();
    file.setFileName("dorothy2_middling_140714.zip");

    assertThat(file.getVersionAsDouble()).isEqualTo(140714.0);
  }

  @Test
  public void getIdReturnsId4() {
    Package file = new Package();
    file.setFileName("DOA.0.26.zip");

    assertThat(file.getId()).isEqualTo("DOA");
  }

  @Test
  public void getVersionReturnsVerson4() {
    Package file = new Package();
    file.setFileName("DOA.0.26.zip");

    assertThat(file.getVersionAsDouble()).isEqualTo(0.26);
  }

  @Test
  public void getIdReturnsId5() {
    Package file = new Package();
    file.setFileName("instagram.27.140.201112162000.rar");

    assertThat(file.getId()).isEqualTo("instagram");
  }

  @Test
  public void getVersionReturnsVerson5() {
    Package file = new Package();
    file.setFileName("instagram.27.140.201112162000.rar");

    assertThat(file.getVersionAsDouble()).isEqualTo(27.140201112162000);
  }

  @Test
  public void getIdReturnsId6() {
    Package file = new Package();
    file.setFileName("xor130224.zip");

    assertThat(file.getId()).isEqualTo("xor");
  }

  @Test
  public void getVersionReturnsVerson6() {
    Package file = new Package();
    file.setFileName("xor130224.zip");

    assertThat(file.getVersionAsDouble()).isEqualTo(130224.0);
  }

  @Test
  public void getIdReturnsId7() {
    Package file = new Package();
    file.setFileName("DOA018.zip");

    assertThat(file.getId()).isEqualTo("DOA");
  }

  @Test
  public void getVersionReturnsVerson7() {
    Package file = new Package();
    file.setFileName("DOA018.zip");

    assertThat(file.getVersionAsDouble()).isEqualTo(0.18);
  }
}
