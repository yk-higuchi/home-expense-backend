package dev.higuchi.homeexpense.presentation.api.balance.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.presentation.api.balance.PeriodResponse;
import dev.higuchi.homeexpense.query.balance.statistics.AttributeAmount;
import dev.higuchi.homeexpense.query.balance.statistics.AttributeBreakdown;
import dev.higuchi.homeexpense.query.balance.statistics.BalanceStatistics;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record BalanceStatisticsResponse(
    @JsonProperty("period") PeriodResponse period,
    @JsonProperty("income") AttributeBreakdownResponse income,
    @JsonProperty("fixed_expense") AttributeBreakdownResponse fixedExpense,
    @JsonProperty("variable_expense") AttributeBreakdownResponse variableExpense,
    @JsonProperty("expense_total") long expenseTotal,
    @JsonProperty("net") long net) {

  public static BalanceStatisticsResponse of(BalanceStatistics statistics) {
    return new BalanceStatisticsResponse(
        new PeriodResponse(statistics.from(), statistics.to()),
        AttributeBreakdownResponse.of(statistics.income()),
        AttributeBreakdownResponse.of(statistics.fixedExpense()),
        AttributeBreakdownResponse.of(statistics.variableExpense()),
        statistics.expenseTotal(),
        statistics.net());
  }

  @RegisterForReflection
  public record AttributeBreakdownResponse(
      @JsonProperty("total") long total,
      @JsonProperty("breakdown") List<AttributeAmountResponse> breakdown) {

    static AttributeBreakdownResponse of(AttributeBreakdown breakdown) {
      return new AttributeBreakdownResponse(
          breakdown.total(),
          breakdown.amounts().stream().map(AttributeAmountResponse::of).toList());
    }
  }

  @RegisterForReflection
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
