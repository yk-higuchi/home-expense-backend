package dev.yhiguchi.home_expense.query.balance.statistics;

import static org.junit.jupiter.api.Assertions.*;

import dev.yhiguchi.home_expense.query.InvalidQueryCriteriaException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BalanceStatisticsCriteriaTest {

  @Test
  void 年のみ指定_期間は1月1日から12月31日() {
    BalanceStatisticsCriteria criteria = new BalanceStatisticsCriteria(2026, null);

    assertEquals(LocalDate.of(2026, 1, 1), criteria.from());
    assertEquals(LocalDate.of(2026, 12, 31), criteria.to());
  }

  @Test
  void 年月指定_期間はその月の初日から末日() {
    BalanceStatisticsCriteria criteria = new BalanceStatisticsCriteria(2026, 4);

    assertEquals(LocalDate.of(2026, 4, 1), criteria.from());
    assertEquals(LocalDate.of(2026, 4, 30), criteria.to());
  }

  @Test
  void 年月指定_うるう年の2月は29日まで() {
    BalanceStatisticsCriteria criteria = new BalanceStatisticsCriteria(2028, 2);

    assertEquals(LocalDate.of(2028, 2, 29), criteria.to());
  }

  @Test
  void 年の下限1と上限2200は許容() {
    assertDoesNotThrow(() -> new BalanceStatisticsCriteria(1, null));
    assertDoesNotThrow(() -> new BalanceStatisticsCriteria(2200, 12));
  }

  @Test
  void 年が0なら例外() {
    assertThrows(InvalidQueryCriteriaException.class, () -> new BalanceStatisticsCriteria(0, null));
  }

  @Test
  void 年が2201なら例外() {
    assertThrows(
        InvalidQueryCriteriaException.class, () -> new BalanceStatisticsCriteria(2201, null));
  }

  @Test
  void 月が0なら例外() {
    assertThrows(InvalidQueryCriteriaException.class, () -> new BalanceStatisticsCriteria(2026, 0));
  }

  @Test
  void 月が13なら例外() {
    assertThrows(
        InvalidQueryCriteriaException.class, () -> new BalanceStatisticsCriteria(2026, 13));
  }
}
