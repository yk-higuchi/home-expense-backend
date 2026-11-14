package dev.higuchi.homeexpense.application.usecase.expense;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.*;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeNotFoundException;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseAttributeRepository;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExpenseUpdateServiceTest {

  InMemoryExpenseRepository expenseRepository = new InMemoryExpenseRepository();
  InMemoryExpenseAttributeRepository attributeRepository = new InMemoryExpenseAttributeRepository();
  ExpenseUpdateService sut = new ExpenseUpdateService(expenseRepository, attributeRepository);

  ExpenseAttribute attribute =
      new ExpenseAttribute(
          new ExpenseAttributeIdentifier("attr-1"),
          new ExpenseAttributeName("食費"),
          ExpenseCategory.変動費);

  @Test
  void 経費を更新する() {
    Expense existing =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    expenseRepository.register(existing);
    attributeRepository.register(attribute);

    sut.update(
        new ExpenseUpdateCommand(
            new ExpenseIdentifier("exp-1"),
            new Description("ディナー"),
            new Amount(2000),
            new PaymentDate(LocalDate.of(2026, 4, 2)),
            new ExpenseAttributeIdentifier("attr-1"),
            ExpectedVersion.of(1L)));

    Expense updated = expenseRepository.find(new ExpenseIdentifier("exp-1")).orElseThrow();
    assertEquals("ディナー", updated.description().value());
    assertEquals(2000, updated.amount().value());
    assertEquals(2L, expenseRepository.versionOf(new ExpenseIdentifier("exp-1")));
  }

  @Test
  void 変更がない場合は更新しない() {
    Expense existing =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    expenseRepository.register(existing);
    attributeRepository.register(attribute);

    sut.update(
        new ExpenseUpdateCommand(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"),
            ExpectedVersion.of(1L)));

    Expense result = expenseRepository.find(new ExpenseIdentifier("exp-1")).orElseThrow();
    assertSame(existing, result);
    assertEquals(1L, expenseRepository.versionOf(new ExpenseIdentifier("exp-1")));
  }

  @Test
  void 存在しない経費を更新すると例外をスローする() {
    attributeRepository.register(attribute);

    assertThrows(
        ExpenseNotFoundException.class,
        () ->
            sut.update(
                new ExpenseUpdateCommand(
                    new ExpenseIdentifier("not-exist"),
                    new Description("ランチ"),
                    new Amount(1000),
                    new PaymentDate(LocalDate.of(2026, 4, 1)),
                    new ExpenseAttributeIdentifier("attr-1"),
                    ExpectedVersion.of(1L))));
  }

  @Test
  void 存在しない属性IDで更新すると例外をスローする() {
    Expense existing =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    expenseRepository.register(existing);

    assertThrows(
        ExpenseAttributeNotFoundException.class,
        () ->
            sut.update(
                new ExpenseUpdateCommand(
                    new ExpenseIdentifier("exp-1"),
                    new Description("ディナー"),
                    new Amount(2000),
                    new PaymentDate(LocalDate.of(2026, 4, 2)),
                    new ExpenseAttributeIdentifier("not-exist"),
                    ExpectedVersion.of(1L))));
  }

  @Test
  void 変更がなくてもバージョン不一致なら楽観ロック例外() {
    Expense existing =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ"),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    expenseRepository.register(existing);
    attributeRepository.register(attribute);

    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.update(
                new ExpenseUpdateCommand(
                    new ExpenseIdentifier("exp-1"),
                    new Description("ランチ"),
                    new Amount(1000),
                    new PaymentDate(LocalDate.of(2026, 4, 1)),
                    new ExpenseAttributeIdentifier("attr-1"),
                    ExpectedVersion.of(2L))));
  }
}
