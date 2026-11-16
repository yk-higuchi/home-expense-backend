package dev.higuchi.homeexpense.query.balance.series;

import java.time.LocalDate;

public record BalanceSeriesPoint(
    LocalDate from,
    LocalDate to,
    long incomeTotal,
    long fixedExpenseTotal,
    long variableExpenseTotal) {

  public long expenseTotal() {
    return Math.addExact(fixedExpenseTotal, variableExpenseTotal);
  }

  public long net() {
    return Math.subtractExact(incomeTotal, expenseTotal());
  }
}
