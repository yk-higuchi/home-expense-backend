package dev.higuchi.homeexpense.query.expense;

import dev.higuchi.homeexpense.query.Pagination;

public class ExpenseCriteriaCreator {

  public static ExpenseSearchCriteria create(
      Integer page,
      Integer perPage,
      Integer year,
      Integer month,
      String category,
      String attributeId) {
    Pagination pagination = new Pagination(page, perPage);
    return new ExpenseSearchCriteria(pagination, year, month, category, attributeId);
  }
}
