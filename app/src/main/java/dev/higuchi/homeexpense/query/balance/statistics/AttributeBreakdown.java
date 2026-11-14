package dev.higuchi.homeexpense.query.balance.statistics;

import java.util.List;

/** 属性別の内訳（収入属性別、または経費分類ごとの経費属性別） */
public record AttributeBreakdown(List<AttributeAmount> amounts) {

  public AttributeBreakdown {
    amounts = List.copyOf(amounts);
  }

  public static AttributeBreakdown empty() {
    return new AttributeBreakdown(List.of());
  }

  public long total() {
    return amounts.stream().mapToLong(AttributeAmount::amount).reduce(0L, Math::addExact);
  }
}
