package dev.higuchi.homeexpense.query.balance.statistics;

public interface BalanceStatisticsQuerier {

  BalanceStatistics search(BalanceStatisticsCriteria criteria);
}
