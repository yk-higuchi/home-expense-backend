package dev.higuchi.homeexpense.presentation.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IsoLocalDateValidatorTest {

  private final IsoLocalDateValidator sut = new IsoLocalDateValidator();

  @Test
  void 実在する日付は妥当() {
    assertTrue(sut.isValid("2026-02-28", null));
    assertTrue(sut.isValid("2028-02-29", null));
  }

  @Test
  void nullは対象外() {
    assertTrue(sut.isValid(null, null));
  }

  @Test
  void 存在しない日付は不正() {
    assertFalse(sut.isValid("2026-02-30", null));
    assertFalse(sut.isValid("2026-02-29", null));
    assertFalse(sut.isValid("2026-13-01", null));
  }

  @Test
  void 形式違いは不正() {
    assertFalse(sut.isValid("2026/02/01", null));
    assertFalse(sut.isValid("2026-2-1", null));
    assertFalse(sut.isValid("+2026-02-01", null));
  }
}
