package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeDetail;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record IncomeAttributeGetListResponse(
    @JsonProperty("income_attributes") List<IncomeAttributeGetResponse> list) {

  public static IncomeAttributeGetListResponse from(List<IncomeAttributeDetail> details) {
    return new IncomeAttributeGetListResponse(
        details.stream().map(IncomeAttributeGetResponse::from).toList());
  }
}
