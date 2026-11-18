package dev.higuchi.homeexpense.presentation.api.income;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.income.IncomeSearchResult;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record IncomeGetListResponse(@JsonProperty("incomes") List<IncomeGetResponse> list) {

  public static IncomeGetListResponse from(IncomeSearchResult searchResult, int page) {
    if (page > searchResult.totalCount()) {
      return new IncomeGetListResponse(List.of());
    }
    return new IncomeGetListResponse(
        searchResult.list().stream().map(IncomeGetResponse::from).toList());
  }
}
