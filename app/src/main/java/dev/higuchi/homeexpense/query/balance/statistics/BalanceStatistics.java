package dev.higuchi.homeexpense.query.balance.statistics;

import java.time.LocalDate;

public record BalanceStatistics(
    LocalDate from,
    LocalDate to,
    AttributeBreakdown income,
    AttributeBreakdown fixedExpense,
    AttributeBreakdown variableExpense) {

  public long expenseTotal() {
    return Math.addExact(fixedExpense.total(), variableExpense.total());
  }

  public long net() {
    return Math.subtractExact(income.total(), expenseTotal());
  }
}
