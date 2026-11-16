package dev.higuchi.homeexpense.core.model.expense.attribute;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ExpenseAttributeNameTest {

  @Test
  void nullは例外をスローする() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new ExpenseAttributeName(null));
    assertEquals("支出属性名は必須です", exception.getMessage());
  }
}
