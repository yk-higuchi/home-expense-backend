package dev.higuchi.homeexpense.core.model.income.attribute;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IncomeAttributeOrderTest {

  IncomeAttributeIdentifier a = new IncomeAttributeIdentifier("a");
  IncomeAttributeIdentifier b = new IncomeAttributeIdentifier("b");

  @Test
  void ダイジェストは並べ替えのPUTの本文と同じ形のドキュメントから計算する() {
    assertEquals(
        DocumentDigest.of(Map.of("ids", List.of("b", "a"))),
        new IncomeAttributeOrder(List.of(b, a)).digest());
  }

  @Test
  void 同じ属性を過不足なく含むかを判定する() {
    IncomeAttributeOrder current = new IncomeAttributeOrder(List.of(a, b));
    assertTrue(new IncomeAttributeOrder(List.of(b, a)).hasSameMembersAs(current));
    assertFalse(new IncomeAttributeOrder(List.of(a)).hasSameMembersAs(current));
    assertFalse(new IncomeAttributeOrder(List.of(a, a)).hasSameMembersAs(current));
  }
}
