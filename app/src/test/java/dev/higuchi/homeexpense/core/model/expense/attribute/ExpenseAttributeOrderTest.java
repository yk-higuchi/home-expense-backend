package dev.higuchi.homeexpense.core.model.expense.attribute;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExpenseAttributeOrderTest {

  ExpenseAttributeIdentifier a = new ExpenseAttributeIdentifier("a");
  ExpenseAttributeIdentifier b = new ExpenseAttributeIdentifier("b");
  ExpenseAttributeIdentifier c = new ExpenseAttributeIdentifier("c");

  @Test
  void ダイジェストは並べ替えのPUTの本文と同じ形のドキュメントから計算する() {
    assertEquals(
        DocumentDigest.of(Map.of("category", "固定費", "ids", List.of("b", "a"))),
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(b, a)).digest());
  }

  @Test
  void 並びが変わるとダイジェストも変わる() {
    assertNotEquals(
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(a, b)).digest(),
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(b, a)).digest());
  }

  @Test
  void 同じ属性を過不足なく含むかを判定する() {
    ExpenseAttributeOrder current = new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(a, b));
    assertTrue(
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(b, a)).hasSameMembersAs(current));
    assertFalse(
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(a)).hasSameMembersAs(current));
    assertFalse(
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(a, b, c)).hasSameMembersAs(current));
    assertFalse(
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(a, c)).hasSameMembersAs(current));
    assertFalse(
        new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(a, a)).hasSameMembersAs(current));
    assertFalse(
        new ExpenseAttributeOrder(ExpenseCategory.変動費, List.of(a, b)).hasSameMembersAs(current));
  }
}
