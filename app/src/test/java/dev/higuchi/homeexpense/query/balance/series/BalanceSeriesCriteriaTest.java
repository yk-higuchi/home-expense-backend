package dev.higuchi.homeexpense.query.balance.series;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.query.Granularity;
import dev.higuchi.homeexpense.query.InvalidQueryCriteriaException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class BalanceSeriesCriteriaTest {

  @Test
  void from_to順序違反で例外() {
    assertThrows(
        InvalidQueryCriteriaException.class,
        () ->
            new BalanceSeriesCriteria(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 1, 1), Granularity.MONTH));
  }

  @Test
  void DAY粒度で1年超過時に例外() {
    assertThrows(
        InvalidQueryCriteriaException.class,
        () ->
            new BalanceSeriesCriteria(
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 12, 31), Granularity.DAY));
  }

  @Test
  void MONTH粒度で12ヶ月は許容() {
    assertDoesNotThrow(() ->
        new BalanceSeriesCriteria(
            LocalDate.of(2025, 5, 1), LocalDate.of(2026, 4, 30), Granularity.MONTH));
  }

  @Test
  void YEAR粒度で20年は許容() {
    assertDoesNotThrow(() ->
        new BalanceSeriesCriteria(
            LocalDate.of(2007, 1, 1), LocalDate.of(2026, 12, 31), Granularity.YEAR));
  }

  @Test
  void DAY粒度で365日は許容() {
    assertDoesNotThrow(() ->
        new BalanceSeriesCriteria(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), Granularity.DAY));
  }

  @Test
  void from_to同日は許容() {
    assertDoesNotThrow(() ->
        new BalanceSeriesCriteria(
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 1), Granularity.DAY));
  }
}
