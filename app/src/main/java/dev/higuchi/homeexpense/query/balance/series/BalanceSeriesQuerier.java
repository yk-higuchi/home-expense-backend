package dev.higuchi.homeexpense.query.balance.series;

public interface BalanceSeriesQuerier {

  BalanceSeries search(BalanceSeriesCriteria criteria);
}
