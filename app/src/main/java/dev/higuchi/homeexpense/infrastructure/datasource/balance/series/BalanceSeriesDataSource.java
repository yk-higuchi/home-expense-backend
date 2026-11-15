package dev.higuchi.homeexpense.infrastructure.datasource.balance.series;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.infrastructure.datasource.ReadOnly;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import dev.higuchi.homeexpense.query.Granularity;
import dev.higuchi.homeexpense.query.balance.series.BalanceSeries;
import dev.higuchi.homeexpense.query.balance.series.BalanceSeriesCriteria;
import dev.higuchi.homeexpense.query.balance.series.BalanceSeriesPoint;
import dev.higuchi.homeexpense.query.balance.series.BalanceSeriesQuerier;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BalanceSeriesDataSource implements BalanceSeriesQuerier {

  private static final String SQL_TEMPLATE =
      """
      WITH bounds AS (
        SELECT
          date_trunc('%1$s', ?::date)::date AS range_start,
          (date_trunc('%1$s', ?::date) + INTERVAL '%2$s')::date AS range_end
      ),
      periods AS (
        SELECT generate_series(
          b.range_start::timestamp,
          (b.range_end - INTERVAL '%2$s')::timestamp,
          INTERVAL '%2$s'
        )::date AS period_start
        FROM bounds b
      ),
      income_totals AS (
        SELECT date_trunc('%1$s', i.receive_date)::date AS period_start, SUM(i.amount) AS income_total
        FROM expense.income i, bounds b
        WHERE i.receive_date >= b.range_start AND i.receive_date < b.range_end
        GROUP BY 1
      ),
      expense_totals AS (
        SELECT
          date_trunc('%1$s', e.payment_date)::date AS period_start,
          SUM(CASE WHEN a.category = '%3$s' THEN e.amount ELSE 0 END) AS fixed_total,
          SUM(CASE WHEN a.category = '%4$s' THEN e.amount ELSE 0 END) AS variable_total
        FROM expense.expense e
        INNER JOIN expense.attribute a ON e.attribute_id = a.id
        CROSS JOIN bounds b
        WHERE e.payment_date >= b.range_start AND e.payment_date < b.range_end
        GROUP BY 1
      )
      SELECT
        p.period_start,
        (p.period_start + INTERVAL '%2$s' - INTERVAL '1 day')::date AS period_end,
        COALESCE(it.income_total, 0) AS income_total,
        COALESCE(et.fixed_total, 0) AS fixed_total,
        COALESCE(et.variable_total, 0) AS variable_total
      FROM periods p
      LEFT JOIN income_totals it ON it.period_start = p.period_start
      LEFT JOIN expense_totals et ON et.period_start = p.period_start
      ORDER BY p.period_start
      """;

  private final JdbcOperator jdbc;

  public BalanceSeriesDataSource(@ReadOnly JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public BalanceSeries search(BalanceSeriesCriteria criteria) {
    String sql =
        SQL_TEMPLATE.formatted(
            dateTruncUnit(criteria.granularity()),
            interval(criteria.granularity()),
            ExpenseCategory.固定費.name(),
            ExpenseCategory.変動費.name());

    List<BalanceSeriesPoint> points =
        jdbc.queryForList(
            sql,
            ParameterBinder.of(criteria.from(), criteria.to()),
            rs ->
                new BalanceSeriesPoint(
                    rs.getObject("period_start", LocalDate.class),
                    rs.getObject("period_end", LocalDate.class),
                    rs.getLong("income_total"),
                    rs.getLong("fixed_total"),
                    rs.getLong("variable_total")));

    return new BalanceSeries(criteria.granularity(), points);
  }

  private static String dateTruncUnit(Granularity granularity) {
    return switch (granularity) {
      case DAY -> "day";
      case WEEK -> "week";
      case MONTH -> "month";
      case QUARTER -> "quarter";
      case YEAR -> "year";
    };
  }

  private static String interval(Granularity granularity) {
    return switch (granularity) {
      case DAY -> "1 day";
      case WEEK -> "7 days";
      case MONTH -> "1 month";
      case QUARTER -> "3 months";
      case YEAR -> "1 year";
    };
  }
}
