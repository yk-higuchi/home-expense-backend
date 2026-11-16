package dev.higuchi.homeexpense.core.model.expense;

import java.util.Objects;

public record ExpenseIdentifier(String value) {
  public ExpenseIdentifier {
    Objects.requireNonNull(value, "支出識別子は必須です");
  }
}
