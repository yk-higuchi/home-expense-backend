package dev.yhiguchi.home_expense.query;

public enum Granularity {
  DAY,
  WEEK,
  MONTH,
  QUARTER,
  YEAR;

  public String sqlInterval() {
    return name().toLowerCase();
  }
}
