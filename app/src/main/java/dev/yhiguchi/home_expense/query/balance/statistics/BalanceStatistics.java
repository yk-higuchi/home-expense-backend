package dev.yhiguchi.home_expense.query.balance.statistics;

import java.time.LocalDate;

/** 期間内の収支統計 */
public record BalanceStatistics(
    LocalDate from,
    LocalDate to,
    CategoryBreakdown income,
    CategoryBreakdown fixedExpense,
    CategoryBreakdown variableExpense) {

  public long expenseTotal() {
    return Math.addExact(fixedExpense.total(), variableExpense.total());
  }

  public long balance() {
    return Math.subtractExact(income.total(), expenseTotal());
  }
}
