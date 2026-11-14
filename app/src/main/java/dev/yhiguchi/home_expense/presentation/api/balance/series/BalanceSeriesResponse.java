package dev.yhiguchi.home_expense.presentation.api.balance.series;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeries;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesPoint;
import java.time.LocalDate;
import java.util.List;

public record BalanceSeriesResponse(
    @JsonProperty("granularity") String granularity,
    @JsonProperty("items") List<PointResponse> items) {

  public static BalanceSeriesResponse of(BalanceSeries series) {
    return new BalanceSeriesResponse(
        series.granularity().name().toLowerCase(),
        series.items().stream().map(PointResponse::of).toList());
  }

  public record PointResponse(
      @JsonProperty("period_start") LocalDate periodStart,
      @JsonProperty("period_end") LocalDate periodEnd,
      @JsonProperty("income_total") long incomeTotal,
      @JsonProperty("fixed_expense_total") long fixedExpenseTotal,
      @JsonProperty("variable_expense_total") long variableExpenseTotal,
      @JsonProperty("expense_total") long expenseTotal,
      @JsonProperty("balance") long balance) {

    static PointResponse of(BalanceSeriesPoint point) {
      return new PointResponse(
          point.periodStart(),
          point.periodEnd(),
          point.incomeTotal(),
          point.fixedExpenseTotal(),
          point.variableExpenseTotal(),
          point.expenseTotal(),
          point.balance());
    }
  }
}
