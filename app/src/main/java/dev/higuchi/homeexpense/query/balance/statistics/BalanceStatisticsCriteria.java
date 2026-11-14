package dev.higuchi.homeexpense.query.balance.statistics;

import dev.higuchi.homeexpense.query.InvalidQueryCriteriaException;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 収支統計の検索条件。month が null の場合は年間、指定された場合はその月を対象とする。
 *
 * @param year 対象年 (1〜2200)
 * @param month 対象月 (1〜12、任意)
 */
public record BalanceStatisticsCriteria(int year, Integer month) {

  public static final int MIN_YEAR = 1;
  public static final int MAX_YEAR = 2200;

  public BalanceStatisticsCriteria {
    if (year < MIN_YEAR || year > MAX_YEAR) {
      throw new InvalidQueryCriteriaException("yearは%d〜%dを指定してください".formatted(MIN_YEAR, MAX_YEAR));
    }
    if (month != null && (month < 1 || month > 12)) {
      throw new InvalidQueryCriteriaException("monthは1〜12を指定してください");
    }
  }

  /** 期間の開始日（含む） */
  public LocalDate from() {
    return month == null ? LocalDate.of(year, 1, 1) : YearMonth.of(year, month).atDay(1);
  }

  /** 期間の終了日（含む） */
  public LocalDate to() {
    return month == null ? LocalDate.of(year, 12, 31) : YearMonth.of(year, month).atEndOfMonth();
  }
}
