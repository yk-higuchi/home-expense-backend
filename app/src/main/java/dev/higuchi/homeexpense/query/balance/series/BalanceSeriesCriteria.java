package dev.higuchi.homeexpense.query.balance.series;

import dev.higuchi.homeexpense.query.Granularity;
import dev.higuchi.homeexpense.query.InvalidQueryCriteriaException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

public record BalanceSeriesCriteria(LocalDate from, LocalDate to, Granularity granularity) {

  public static final int MAX_POINTS = 366;

  public BalanceSeriesCriteria {
    if (from.isAfter(to)) {
      throw new InvalidQueryCriteriaException("fromはto以前である必要があります");
    }
    int points = estimatePoints(from, to, granularity);
    if (points > MAX_POINTS) {
      throw new InvalidQueryCriteriaException(
          "結果が%dポイントを超えます(推定: %d)。期間を狭めるか粒度を粗くしてください".formatted(MAX_POINTS, points));
    }
  }

  private static int estimatePoints(LocalDate from, LocalDate to, Granularity g) {
    return switch (g) {
      case DAY -> (int) ChronoUnit.DAYS.between(from, to) + 1;
      case WEEK -> (int) ChronoUnit.WEEKS.between(from, to) + 1;
      case MONTH -> (int) ChronoUnit.MONTHS.between(YearMonth.from(from), YearMonth.from(to)) + 1;
      case QUARTER ->
          (int) ChronoUnit.MONTHS.between(YearMonth.from(from), YearMonth.from(to)) / 3 + 1;
      case YEAR -> to.getYear() - from.getYear() + 1;
    };
  }
}
