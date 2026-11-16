package dev.higuchi.homeexpense.core.model.income;

import java.util.Objects;

public record IncomeIdentifier(String value) {
  public IncomeIdentifier {
    Objects.requireNonNull(value, "収入識別子は必須です");
  }
}
