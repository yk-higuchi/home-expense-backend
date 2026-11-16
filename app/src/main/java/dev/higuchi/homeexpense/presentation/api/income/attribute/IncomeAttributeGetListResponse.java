package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeDetail;
import java.util.List;

public record IncomeAttributeGetListResponse(
    @JsonProperty("income_attributes") List<IncomeAttributeGetResponse> list) {

  public static IncomeAttributeGetListResponse from(List<IncomeAttributeDetail> details) {
    return new IncomeAttributeGetListResponse(
        details.stream().map(IncomeAttributeGetResponse::from).toList());
  }
}
