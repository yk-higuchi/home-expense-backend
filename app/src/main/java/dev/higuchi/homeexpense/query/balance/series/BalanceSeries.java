package dev.higuchi.homeexpense.query.balance.series;

import dev.higuchi.homeexpense.query.Granularity;
import java.util.List;

public record BalanceSeries(Granularity granularity, List<BalanceSeriesPoint> items) {}
