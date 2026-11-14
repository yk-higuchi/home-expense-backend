package dev.higuchi.homeexpense.query.income.attribute;

import dev.higuchi.homeexpense.query.Pagination;

public class IncomeAttributeSearchCriteria {
  Pagination pagination;

  public IncomeAttributeSearchCriteria(Pagination pagination) {
    this.pagination = pagination;
  }

  public int offset() {
    return pagination.offset();
  }

  public int perPage() {
    return pagination.perPage();
  }
}
