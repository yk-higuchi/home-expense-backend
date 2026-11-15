package dev.higuchi.homeexpense.application.usecase.expense;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.*;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExpenseDeletionServiceTest {

  InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
  ExpenseDeletionService sut = new ExpenseDeletionService(expenseRepository);

  @Test
  void 経費を削除する() {
    Expense existing =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    expenseRepository.register(existing);

    sut.delete(new ExpenseIdentifier("exp-1"), ExpectedDigest.of(existing.digest()));

    assertTrue(expenseRepository.all().isEmpty());
  }

  @Test
  void 存在しない経費を削除すると例外をスローする() {
    assertThrows(
        ExpenseNotFoundException.class,
        () -> sut.delete(new ExpenseIdentifier("not-exist"), ExpectedDigest.of(TestDigests.STALE)));
  }

  @Test
  void バージョンが一致しない経費は削除せず例外をスローする() {
    Expense existing =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    expenseRepository.register(existing);

    assertThrows(
        ConcurrentUpdateException.class,
        () -> sut.delete(new ExpenseIdentifier("exp-1"), ExpectedDigest.of(TestDigests.STALE)));
    assertEquals(1, expenseRepository.all().size());
  }
}
