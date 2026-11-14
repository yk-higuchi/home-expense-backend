package dev.yhiguchi.home_expense.query.balance.series;

import dev.yhiguchi.home_expense.query.Granularity;
import java.util.List;

public record BalanceSeries(Granularity granularity, List<BalanceSeriesPoint> items) {}
