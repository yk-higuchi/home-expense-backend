package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeDetail;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ExpenseAttributeGetResponse(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("category") String category) {
  static ExpenseAttributeGetResponse from(ExpenseAttributeDetail detail) {
    return new ExpenseAttributeGetResponse(detail.id(), detail.name(), detail.category());
  }
}
