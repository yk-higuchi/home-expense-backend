package dev.higuchi.homeexpense.query.balance.series;

import java.time.LocalDate;

/**
 * 時系列の 1 期間分の収支
 *
 * @param from 期間の開始日（含む）
 * @param to 期間の終了日（含む）
 */
public record BalanceSeriesPoint(
    LocalDate from,
    LocalDate to,
    long incomeTotal,
    long fixedExpenseTotal,
    long variableExpenseTotal) {

  /** 経費合計（固定費 + 変動費） */
  public long expenseTotal() {
    return Math.addExact(fixedExpenseTotal, variableExpenseTotal);
  }

  /** 純収支（収入 - 経費）。経費超過ならマイナス */
  public long net() {
    return Math.subtractExact(incomeTotal, expenseTotal());
  }
}
