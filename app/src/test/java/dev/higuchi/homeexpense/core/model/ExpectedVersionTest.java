package dev.higuchi.homeexpense.core.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ExpectedVersionTest {

  @Test
  void 単一バージョンは一致するときだけtrue() {
    assertTrue(ExpectedVersion.of(3L).matches(3L));
    assertFalse(ExpectedVersion.of(3L).matches(4L));
  }

  @Test
  void 複数バージョンはいずれかと一致すればtrue() {
    ExpectedVersion expected = ExpectedVersion.oneOf(Set.of(1L, 3L));

    assertTrue(expected.matches(3L));
    assertFalse(expected.matches(2L));
  }

  @Test
  void 空の候補はどのバージョンとも一致しない() {
    assertFalse(ExpectedVersion.oneOf(Set.of()).matches(1L));
  }

  @Test
  void バージョンを問わない指定は常にtrue() {
    assertTrue(ExpectedVersion.unconditional().matches(42L));
  }
}
