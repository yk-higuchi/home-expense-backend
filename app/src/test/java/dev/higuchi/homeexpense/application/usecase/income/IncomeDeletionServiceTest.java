package dev.higuchi.homeexpense.application.usecase.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.income.*;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class IncomeDeletionServiceTest {

  InMemoryIncomeRepository incomeRepository = new InMemoryIncomeRepository();
  IncomeDeletionService sut = new IncomeDeletionService(incomeRepository);

  @Test
  void 収入を削除する() {
    Income existing =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    incomeRepository.register(existing);

    sut.delete(new IncomeIdentifier("inc-1"), ExpectedVersion.of(1));

    assertTrue(incomeRepository.all().isEmpty());
  }

  @Test
  void 存在しない収入を削除すると例外をスローする() {
    assertThrows(
        IncomeNotFoundException.class,
        () -> sut.delete(new IncomeIdentifier("not-exist"), ExpectedVersion.of(1)));
  }

  @Test
  void バージョンが一致しない収入は削除せず例外をスローする() {
    Income existing =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    incomeRepository.register(existing);

    assertThrows(
        ConcurrentUpdateException.class,
        () -> sut.delete(new IncomeIdentifier("inc-1"), ExpectedVersion.of(2L)));
    assertTrue(incomeRepository.find(new IncomeIdentifier("inc-1")).isPresent());
  }
}
