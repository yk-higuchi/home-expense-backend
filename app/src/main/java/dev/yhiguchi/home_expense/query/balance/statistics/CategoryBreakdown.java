package dev.yhiguchi.home_expense.query.balance.statistics;

import java.util.List;

/** カテゴリ（収入・固定費・変動費）の属性別内訳 */
public record CategoryBreakdown(List<AttributeAmount> breakdown) {

  public CategoryBreakdown {
    breakdown = List.copyOf(breakdown);
  }

  public static CategoryBreakdown empty() {
    return new CategoryBreakdown(List.of());
  }

  public long total() {
    return breakdown.stream().mapToLong(AttributeAmount::amount).reduce(0L, Math::addExact);
  }
}
