package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeDetail;

public record IncomeAttributeGetResponse(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("version") long version) {
  static IncomeAttributeGetResponse from(IncomeAttributeDetail detail) {
    return new IncomeAttributeGetResponse(detail.id(), detail.name(), detail.version());
  }
}
