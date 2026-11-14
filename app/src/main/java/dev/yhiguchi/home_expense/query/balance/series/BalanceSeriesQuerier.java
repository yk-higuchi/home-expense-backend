package dev.yhiguchi.home_expense.query.balance.series;

public interface BalanceSeriesQuerier {

  BalanceSeries search(BalanceSeriesCriteria criteria);
}
