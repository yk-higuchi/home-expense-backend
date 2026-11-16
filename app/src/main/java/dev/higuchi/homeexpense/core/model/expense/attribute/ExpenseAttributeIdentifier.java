package dev.higuchi.homeexpense.core.model.expense.attribute;

import java.util.Objects;

public record ExpenseAttributeIdentifier(String value) {
  public ExpenseAttributeIdentifier {
    Objects.requireNonNull(value, "支出属性識別子は必須です");
  }
}
