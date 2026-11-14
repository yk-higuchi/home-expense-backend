package dev.yhiguchi.home_expense.query.balance.series;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BalanceSeriesPointTest {

  private static final LocalDate START = LocalDate.of(2026, 4, 1);
  private static final LocalDate END = LocalDate.of(2026, 4, 30);

  @Test
  void 支出合計は固定費と変動費の和() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 300000L, 152000L, 78000L);

    assertEquals(230000L, point.expenseTotal());
  }

  @Test
  void 収支は収入から支出を引いた値() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 300000L, 152000L, 78000L);

    assertEquals(70000L, point.balance());
  }

  @Test
  void 支出超過なら収支はマイナス() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 0L, 120000L, 0L);

    assertEquals(-120000L, point.balance());
  }

  @Test
  void すべて0なら支出合計も収支も0() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 0L, 0L, 0L);

    assertEquals(0L, point.expenseTotal());
    assertEquals(0L, point.balance());
  }
}
