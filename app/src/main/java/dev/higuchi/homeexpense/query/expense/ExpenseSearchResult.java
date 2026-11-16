package dev.higuchi.homeexpense.query.expense;

import java.util.List;

public record ExpenseSearchResult(Integer totalCount, List<ExpenseDetail> list) {

  public static ExpenseSearchResult empty() {
    return new ExpenseSearchResult(0, List.of());
  }
}
