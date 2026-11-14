package dev.yhiguchi.home_expense.query.balance.statistics;

public interface BalanceStatisticsQuerier {

  BalanceStatistics search(BalanceStatisticsCriteria criteria);
}
