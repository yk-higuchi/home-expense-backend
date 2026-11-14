package dev.higuchi.homeexpense.query.balance.series;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BalanceSeriesPointTest {

  private static final LocalDate START = LocalDate.of(2026, 4, 1);
  private static final LocalDate END = LocalDate.of(2026, 4, 30);

  @Test
  void 経費合計は固定費と変動費の和() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 300000L, 152000L, 78000L);

    assertEquals(230000L, point.expenseTotal());
  }

  @Test
  void 純収支は収入から経費を引いた値() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 300000L, 152000L, 78000L);

    assertEquals(70000L, point.net());
  }

  @Test
  void 経費超過なら純収支はマイナス() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 0L, 120000L, 0L);

    assertEquals(-120000L, point.net());
  }

  @Test
  void すべて0なら経費合計も純収支も0() {
    BalanceSeriesPoint point = new BalanceSeriesPoint(START, END, 0L, 0L, 0L);

    assertEquals(0L, point.expenseTotal());
    assertEquals(0L, point.net());
  }
}
