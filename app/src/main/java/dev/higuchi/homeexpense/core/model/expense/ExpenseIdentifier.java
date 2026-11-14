package dev.higuchi.homeexpense.core.model.expense;

import java.util.Objects;

/** 経費識別子 */
public record ExpenseIdentifier(String value) {
  public ExpenseIdentifier {
    Objects.requireNonNull(value, "経費識別子は必須です");
  }
}
