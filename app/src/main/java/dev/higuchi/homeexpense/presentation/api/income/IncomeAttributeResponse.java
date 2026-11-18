package dev.higuchi.homeexpense.presentation.api.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record IncomeAttributeResponse(
    @JsonProperty("id") String id, @JsonProperty("name") String name) {

  static IncomeAttributeResponse from(IncomeAttribute incomeAttribute) {
    return new IncomeAttributeResponse(
        incomeAttribute.incomeAttributeIdentifier().value(),
        incomeAttribute.incomeAttributeName().value());
  }
}
