package dev.higuchi.homeexpense.query.balance.statistics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class BalanceStatisticsTest {

  private static final LocalDate FROM = LocalDate.of(2026, 4, 1);
  private static final LocalDate TO = LocalDate.of(2026, 4, 30);

  @Test
  void 合計は内訳の金額の和() {
    AttributeBreakdown breakdown =
        new AttributeBreakdown(
            List.of(
                new AttributeAmount("00000000-0000-0000-0000-000000000001", "食費", 50000L),
                new AttributeAmount("00000000-0000-0000-0000-000000000002", "日用品", 30000L)));

    assertEquals(80000L, breakdown.total());
  }

  @Test
  void 内訳が空なら合計は0() {
    assertEquals(0L, AttributeBreakdown.empty().total());
    assertTrue(AttributeBreakdown.empty().amounts().isEmpty());
  }

  @Test
  void 支出合計は固定費と変動費の和() {
    BalanceStatistics statistics = statistics(500000L, 100000L, 200000L);

    assertEquals(300000L, statistics.expenseTotal());
  }

  @Test
  void 純収支は収入から支出を引いた値() {
    BalanceStatistics statistics = statistics(500000L, 100000L, 200000L);

    assertEquals(200000L, statistics.net());
  }

  @Test
  void 支出超過なら純収支はマイナス() {
    BalanceStatistics statistics = statistics(0L, 80000L, 20000L);

    assertEquals(-100000L, statistics.net());
  }

  @Test
  void 支出合計でオーバーフロー時に例外() {
    BalanceStatistics statistics = statistics(0L, Long.MAX_VALUE, 1L);

    assertThrows(ArithmeticException.class, statistics::expenseTotal);
  }

  @Test
  void 純収支でアンダーフロー時に例外() {
    BalanceStatistics statistics = statistics(Long.MIN_VALUE, 1L, 0L);

    assertThrows(ArithmeticException.class, statistics::net);
  }

  private static BalanceStatistics statistics(long income, long fixed, long variable) {
    return new BalanceStatistics(FROM, TO, single(income), single(fixed), single(variable));
  }

  private static AttributeBreakdown single(long amount) {
    return new AttributeBreakdown(
        List.of(new AttributeAmount("00000000-0000-0000-0000-000000000001", "test", amount)));
  }
}
