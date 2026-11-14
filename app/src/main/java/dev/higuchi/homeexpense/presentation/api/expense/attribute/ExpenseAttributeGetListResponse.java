package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchResult;
import java.util.List;

public record ExpenseAttributeGetListResponse(
    @JsonProperty("expense_attributes") List<ExpenseAttributeGetResponse> list) {

  public static ExpenseAttributeGetListResponse from(
      ExpenseAttributeSearchResult searchResult, int page) {
    if (page > searchResult.totalCount()) {
      return new ExpenseAttributeGetListResponse(List.of());
    }
    return new ExpenseAttributeGetListResponse(
        searchResult.list().stream().map(ExpenseAttributeGetResponse::from).toList());
  }
}
