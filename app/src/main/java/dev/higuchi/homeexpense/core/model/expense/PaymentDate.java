package dev.higuchi.homeexpense.core.model.expense;

import java.time.LocalDate;
import java.util.Objects;

public record PaymentDate(LocalDate value) {
  public PaymentDate {
    Objects.requireNonNull(value, "支払日は必須です");
  }

  public String asString() {
    return value.toString();
  }
}
