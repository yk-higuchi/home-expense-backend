package dev.higuchi.homeexpense.core.model.income.attribute;

import java.util.Objects;

public record IncomeAttributeIdentifier(String value) {
  public IncomeAttributeIdentifier {
    Objects.requireNonNull(value, "収入属性識別子は必須です");
  }
}
