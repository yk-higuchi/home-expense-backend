package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeSearchResult;
import java.util.List;

public record IncomeAttributeGetListResponse(
    @JsonProperty("income_attributes") List<IncomeAttributeGetResponse> list) {

  public static IncomeAttributeGetListResponse from(
      IncomeAttributeSearchResult searchResult, int page) {
    if (page > searchResult.totalCount()) {
      return new IncomeAttributeGetListResponse(List.of());
    }
    return new IncomeAttributeGetListResponse(
        searchResult.list().stream().map(IncomeAttributeGetResponse::from).toList());
  }
}
