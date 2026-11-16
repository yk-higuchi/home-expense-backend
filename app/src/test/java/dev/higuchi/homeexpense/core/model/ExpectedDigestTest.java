package dev.higuchi.homeexpense.core.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExpectedDigestTest {

  DocumentDigest a = DocumentDigest.of(Map.of("name", "a"));
  DocumentDigest b = DocumentDigest.of(Map.of("name", "b"));
  DocumentDigest c = DocumentDigest.of(Map.of("name", "c"));

  @Test
  void 単一のダイジェストは一致するときだけtrue() {
    assertTrue(ExpectedDigest.of(a).matches(a));
    assertFalse(ExpectedDigest.of(a).matches(b));
  }

  @Test
  void 複数のダイジェストはいずれかと一致すればtrue() {
    ExpectedDigest expected = ExpectedDigest.oneOf(Set.of(a, c));

    assertTrue(expected.matches(c));
    assertFalse(expected.matches(b));
  }

  @Test
  void 空の候補はどのダイジェストとも一致しない() {
    assertFalse(ExpectedDigest.oneOf(Set.of()).matches(a));
  }

  @Test
  void 条件なしの指定は常にtrue() {
    assertTrue(ExpectedDigest.unconditional().matches(a));
  }
}
