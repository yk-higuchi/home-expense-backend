package dev.higuchi.homeexpense.presentation.api.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.IncomeDetail;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record IncomeGetResponse(
    @JsonProperty("id") String id,
    @JsonProperty("description") String description,
    @JsonProperty("amount") Integer amount,
    @JsonProperty("receive_date") String receiveDate,
    @JsonProperty("income_attribute") IncomeAttributeResponse incomeAttribute) {

  static IncomeGetResponse from(IncomeDetail detail) {
    return new IncomeGetResponse(
        detail.id(),
        detail.description(),
        detail.amount(),
        detail.receiveDate().toString(),
        new IncomeAttributeResponse(detail.attributeId(), detail.attributeName()));
  }
}
