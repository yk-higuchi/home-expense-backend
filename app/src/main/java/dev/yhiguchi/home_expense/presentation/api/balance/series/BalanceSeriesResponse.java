package dev.yhiguchi.home_expense.presentation.api.balance.series;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.yhiguchi.home_expense.presentation.api.balance.PeriodResponse;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeries;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesPoint;
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
      @JsonProperty("period") PeriodResponse period,
      @JsonProperty("income_total") long incomeTotal,
      @JsonProperty("fixed_expense_total") long fixedExpenseTotal,
      @JsonProperty("variable_expense_total") long variableExpenseTotal,
      @JsonProperty("expense_total") long expenseTotal,
      @JsonProperty("net") long net) {

    static PointResponse of(BalanceSeriesPoint point) {
      return new PointResponse(
          new PeriodResponse(point.from(), point.to()),
          point.incomeTotal(),
          point.fixedExpenseTotal(),
          point.variableExpenseTotal(),
          point.expenseTotal(),
          point.net());
    }
  }
}
