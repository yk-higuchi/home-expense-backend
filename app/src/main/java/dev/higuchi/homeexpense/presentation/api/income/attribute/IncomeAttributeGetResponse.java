package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeDetail;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record IncomeAttributeGetResponse(
    @JsonProperty("id") String id, @JsonProperty("name") String name) {
  static IncomeAttributeGetResponse from(IncomeAttributeDetail detail) {
    return new IncomeAttributeGetResponse(detail.id(), detail.name());
  }
}
