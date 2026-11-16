package dev.higuchi.homeexpense.query.income;

import dev.higuchi.homeexpense.query.Pagination;
import java.time.LocalDate;
import java.time.Year;

public class IncomeSearchCriteria {
  Pagination pagination;
  Integer year;

  public IncomeSearchCriteria(Pagination pagination, Integer year) {
    this.pagination = pagination;
    this.year = year;
  }

  public int offset() {
    return pagination.offset();
  }

  public int perPage() {
    return pagination.perPage();
  }

  public Pagination pagination() {
    return pagination;
  }

  public boolean hasDateRange() {
    return year != null;
  }

  public LocalDate dateFrom() {
    return Year.of(year).atDay(1);
  }

  public LocalDate dateTo() {
    return Year.of(year).plusYears(1).atDay(1);
  }
}
