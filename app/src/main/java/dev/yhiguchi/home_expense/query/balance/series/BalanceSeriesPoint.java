package dev.yhiguchi.home_expense.query.balance.series;

import java.time.LocalDate;

/** 時系列の 1 期間分の収支 */
public record BalanceSeriesPoint(
    LocalDate periodStart,
    LocalDate periodEnd,
    long incomeTotal,
    long fixedExpenseTotal,
    long variableExpenseTotal) {

  public long expenseTotal() {
    return Math.addExact(fixedExpenseTotal, variableExpenseTotal);
  }

  public long balance() {
    return Math.subtractExact(incomeTotal, expenseTotal());
  }
}
