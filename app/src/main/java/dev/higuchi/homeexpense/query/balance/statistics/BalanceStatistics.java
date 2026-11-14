package dev.higuchi.homeexpense.query.balance.statistics;

import java.time.LocalDate;

/**
 * 期間内の収支統計
 *
 * @param from 期間の開始日（含む）
 * @param to 期間の終了日（含む）
 */
public record BalanceStatistics(
    LocalDate from,
    LocalDate to,
    AttributeBreakdown income,
    AttributeBreakdown fixedExpense,
    AttributeBreakdown variableExpense) {

  /** 経費合計（固定費 + 変動費） */
  public long expenseTotal() {
    return Math.addExact(fixedExpense.total(), variableExpense.total());
  }

  /** 純収支（収入 - 経費）。経費超過ならマイナス */
  public long net() {
    return Math.subtractExact(income.total(), expenseTotal());
  }
}
