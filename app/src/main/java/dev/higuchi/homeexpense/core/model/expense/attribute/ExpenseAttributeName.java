package dev.higuchi.homeexpense.core.model.expense.attribute;

import java.util.Objects;

public record ExpenseAttributeName(String value) {
  public static final int MAX_LENGTH = 512;

  public ExpenseAttributeName {
    Objects.requireNonNull(value, "支出属性名は必須です");
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("支出属性名は" + MAX_LENGTH + "文字以内でなければなりません");
    }
  }
}
