package dev.yhiguchi.home_expense.presentation.api.balance.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.yhiguchi.home_expense.query.balance.statistics.AttributeAmount;
import dev.yhiguchi.home_expense.query.balance.statistics.BalanceStatistics;
import dev.yhiguchi.home_expense.query.balance.statistics.CategoryBreakdown;
import java.time.LocalDate;
import java.util.List;

public record BalanceStatisticsResponse(
    @JsonProperty("period") PeriodResponse period,
    @JsonProperty("income") CategoryBreakdownResponse income,
    @JsonProperty("fixed_expense") CategoryBreakdownResponse fixedExpense,
    @JsonProperty("variable_expense") CategoryBreakdownResponse variableExpense,
    @JsonProperty("expense_total") long expenseTotal,
    @JsonProperty("balance") long balance) {

  public static BalanceStatisticsResponse of(BalanceStatistics statistics) {
    return new BalanceStatisticsResponse(
        new PeriodResponse(statistics.from(), statistics.to()),
        CategoryBreakdownResponse.of(statistics.income()),
        CategoryBreakdownResponse.of(statistics.fixedExpense()),
        CategoryBreakdownResponse.of(statistics.variableExpense()),
        statistics.expenseTotal(),
        statistics.balance());
  }

  public record PeriodResponse(
      @JsonProperty("from") LocalDate from, @JsonProperty("to") LocalDate to) {}

  public record CategoryBreakdownResponse(
      @JsonProperty("total") long total,
      @JsonProperty("breakdown") List<AttributeAmountResponse> breakdown) {

    static CategoryBreakdownResponse of(CategoryBreakdown category) {
      return new CategoryBreakdownResponse(
          category.total(),
          category.breakdown().stream().map(AttributeAmountResponse::of).toList());
    }
  }

  public record AttributeAmountResponse(
      @JsonProperty("attribute_id") String attributeId,
      @JsonProperty("attribute_name") String attributeName,
      @JsonProperty("amount") long amount) {

    static AttributeAmountResponse of(AttributeAmount amount) {
      return new AttributeAmountResponse(
          amount.attributeId(), amount.attributeName(), amount.amount());
    }
  }
}
