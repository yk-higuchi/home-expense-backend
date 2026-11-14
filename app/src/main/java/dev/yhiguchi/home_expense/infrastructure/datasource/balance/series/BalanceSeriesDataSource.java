package dev.yhiguchi.home_expense.infrastructure.datasource.balance.series;

import dev.yhiguchi.home_expense.domain.model.expense.ExpenseCategory;
import dev.yhiguchi.home_expense.infrastructure.datasource.ReadOnly;
import dev.yhiguchi.home_expense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.yhiguchi.home_expense.infrastructure.datasource.jdbc.ParameterBinder;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeries;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesCriteria;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesPoint;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesQuerier;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class BalanceSeriesDataSource implements BalanceSeriesQuerier {

  // 収入と支出は期間ごとに別々に集計してから結合する（直接 JOIN すると行が掛け合わされ合計が水増しされる）。
  // 集計範囲は from / to を含む期間全体（例: MONTH 粒度で from=4/15 なら 4/1 から）。
  private static final String SQL_TEMPLATE =
      """
      WITH bounds AS (
        SELECT
          date_trunc('%1$s', ?::date)::date AS range_start,
          (date_trunc('%1$s', ?::date) + INTERVAL '1 %1$s')::date AS range_end
      ),
      periods AS (
        SELECT generate_series(
          b.range_start,
          b.range_end - INTERVAL '1 %1$s',
          INTERVAL '1 %1$s'
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
          SUM(CASE WHEN a.category = '%2$s' THEN e.amount ELSE 0 END) AS fixed_total,
          SUM(CASE WHEN a.category = '%3$s' THEN e.amount ELSE 0 END) AS variable_total
        FROM expense.expense e
        INNER JOIN expense.attribute a ON e.attribute_id = a.id
        CROSS JOIN bounds b
        WHERE e.payment_date >= b.range_start AND e.payment_date < b.range_end
        GROUP BY 1
      )
      SELECT
        p.period_start,
        (p.period_start + INTERVAL '1 %1$s' - INTERVAL '1 day')::date AS period_end,
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
            criteria.granularity().sqlInterval(),
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
}
