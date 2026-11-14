package dev.yhiguchi.home_expense.presentation.api;

import static org.junit.jupiter.api.Assertions.*;

import dev.yhiguchi.home_expense.domain.model.ExpectedVersion;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IfMatchParserTest {

  @Test
  void 未指定なら428用の例外() {
    assertThrows(PreconditionRequiredException.class, () -> IfMatchParser.parse(null));
    assertThrows(PreconditionRequiredException.class, () -> IfMatchParser.parse("  "));
  }

  @Test
  void アスタリスクはバージョンを問わない() {
    assertEquals(ExpectedVersion.unconditional(), IfMatchParser.parse("*"));
  }

  @Test
  void 強いETag1つ() {
    assertEquals(ExpectedVersion.of(3L), IfMatchParser.parse("\"3\""));
  }

  @Test
  void 複数のETagはいずれかとの一致として扱う() {
    assertEquals(ExpectedVersion.oneOf(Set.of(1L, 9L)), IfMatchParser.parse("\"9\", \"1\""));
  }

  @Test
  void 空のリスト要素は無視する() {
    assertEquals(ExpectedVersion.oneOf(Set.of(1L, 2L)), IfMatchParser.parse("\"1\", , \"2\""));
  }

  @Test
  void 弱いETagは強い比較で一致しないため候補にしない() {
    assertEquals(ExpectedVersion.oneOf(Set.of()), IfMatchParser.parse("W/\"3\""));
    assertEquals(ExpectedVersion.oneOf(Set.of(4L)), IfMatchParser.parse("W/\"3\", \"4\""));
  }

  @Test
  void 数値以外の強いETagはこのAPIの表現と一致しないため候補にしない() {
    assertEquals(ExpectedVersion.oneOf(Set.of()), IfMatchParser.parse("\"abc\""));
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
