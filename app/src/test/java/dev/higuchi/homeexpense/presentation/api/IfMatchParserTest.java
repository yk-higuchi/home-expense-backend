package dev.higuchi.homeexpense.presentation.api;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IfMatchParserTest {

  DocumentDigest a = DocumentDigest.of(Map.of("name", "a"));
  DocumentDigest b = DocumentDigest.of(Map.of("name", "b"));

  private static String tag(DocumentDigest digest) {
    return "\"" + digest.value() + "\"";
  }

  @Test
  void 未指定なら428用の例外() {
    assertThrows(PreconditionRequiredException.class, () -> IfMatchParser.parse(null));
    assertThrows(PreconditionRequiredException.class, () -> IfMatchParser.parse("  "));
  }

  @Test
  void アスタリスクは条件なし() {
    assertEquals(ExpectedDigest.unconditional(), IfMatchParser.parse("*"));
  }

  @Test
  void 強いETag1つ() {
    assertEquals(ExpectedDigest.of(a), IfMatchParser.parse(tag(a)));
  }

  @Test
  void 複数のETagはいずれかとの一致として扱う() {
    assertEquals(ExpectedDigest.oneOf(Set.of(a, b)), IfMatchParser.parse(tag(b) + ", " + tag(a)));
  }

  @Test
  void 空のリスト要素は無視する() {
    assertEquals(ExpectedDigest.oneOf(Set.of(a, b)), IfMatchParser.parse(tag(a) + ", , " + tag(b)));
  }

  @Test
  void 弱いETagは強い比較で一致しないため候補にしない() {
    assertEquals(ExpectedDigest.oneOf(Set.of()), IfMatchParser.parse("W/" + tag(a)));
    assertEquals(
        ExpectedDigest.oneOf(Set.of(b)), IfMatchParser.parse("W/" + tag(a) + ", " + tag(b)));
  }

  @Test
  void ダイジェストの形式でない強いETagはどの表現とも一致しないため候補にしない() {
    assertEquals(ExpectedDigest.oneOf(Set.of()), IfMatchParser.parse("\"3\""));
    assertEquals(ExpectedDigest.oneOf(Set.of()), IfMatchParser.parse("\"abc\""));
  }

  @Test
  void 引用符のない値は構文不正() {
    assertThrows(InvalidIfMatchHeaderException.class, () -> IfMatchParser.parse("3"));
  }

  @Test
  void 区切りだけの値は構文不正() {
    assertThrows(InvalidIfMatchHeaderException.class, () -> IfMatchParser.parse(","));
  }

  @Test
  void 引用符内の二重引用符は構文不正() {
    assertThrows(InvalidIfMatchHeaderException.class, () -> IfMatchParser.parse("\"a\"b\""));
  }
}
