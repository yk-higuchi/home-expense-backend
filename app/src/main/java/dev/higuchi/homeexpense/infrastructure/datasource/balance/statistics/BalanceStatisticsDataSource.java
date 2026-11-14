package dev.higuchi.homeexpense.infrastructure.datasource.balance.statistics;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.infrastructure.datasource.ReadOnly;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import dev.higuchi.homeexpense.query.balance.statistics.AttributeAmount;
import dev.higuchi.homeexpense.query.balance.statistics.AttributeBreakdown;
import dev.higuchi.homeexpense.query.balance.statistics.BalanceStatistics;
import dev.higuchi.homeexpense.query.balance.statistics.BalanceStatisticsCriteria;
import dev.higuchi.homeexpense.query.balance.statistics.BalanceStatisticsQuerier;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class BalanceStatisticsDataSource implements BalanceStatisticsQuerier {

  private static final String SELECT_INCOME_BY_ATTRIBUTE =
      """
      SELECT ia.id, ia.name, SUM(i.amount) AS amount
      FROM expense.income i
      INNER JOIN expense.income_attribute ia
        ON i.attribute_id = ia.id
      WHERE i.receive_date >= ? AND i.receive_date <= ?
      GROUP BY ia.id, ia.name
      ORDER BY amount DESC, ia.name, ia.id
      """;

  private static final String SELECT_EXPENSE_BY_ATTRIBUTE =
      """
      SELECT a.category, a.id, a.name, SUM(e.amount) AS amount
      FROM expense.expense e
      INNER JOIN expense.attribute a
        ON e.attribute_id = a.id
      WHERE e.payment_date >= ? AND e.payment_date <= ?
      GROUP BY a.category, a.id, a.name
      ORDER BY amount DESC, a.name, a.id
      """;

  private final JdbcOperator jdbc;

  public BalanceStatisticsDataSource(@ReadOnly JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public BalanceStatistics search(BalanceStatisticsCriteria criteria) {
    ParameterBinder period = ParameterBinder.of(criteria.from(), criteria.to());

    List<AttributeAmount> income =
        jdbc.queryForList(
            SELECT_INCOME_BY_ATTRIBUTE,
            period,
            rs ->
                new AttributeAmount(
                    rs.getString("id"), rs.getString("name"), rs.getLong("amount")));

    Map<String, List<AttributeAmount>> expenseByCategory =
        jdbc
            .queryForList(
                SELECT_EXPENSE_BY_ATTRIBUTE,
                period,
                rs ->
                    Map.entry(
                        rs.getString("category"),
                        new AttributeAmount(
                            rs.getString("id"), rs.getString("name"), rs.getLong("amount"))))
            .stream()
            .collect(
                Collectors.groupingBy(
                    Map.Entry::getKey,
                    Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

    return new BalanceStatistics(
        criteria.from(),
        criteria.to(),
        new AttributeBreakdown(income),
        new AttributeBreakdown(
            expenseByCategory.getOrDefault(ExpenseCategory.固定費.name(), List.of())),
        new AttributeBreakdown(
            expenseByCategory.getOrDefault(ExpenseCategory.変動費.name(), List.of())));
  }
}
