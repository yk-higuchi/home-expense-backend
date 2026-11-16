package dev.higuchi.homeexpense.application.usecase.expense;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseAttributeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import org.junit.jupiter.api.Test;

class ExpenseAttributeUpdateServiceTest {

  InMemoryExpenseAttributeRepository attributeRepository = new InMemoryExpenseAttributeRepository();
  ExpenseAttributeUpdateService sut = new ExpenseAttributeUpdateService(attributeRepository);

  @Test
  void 支出属性の名前を更新する() {
    ExpenseAttribute existing =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    attributeRepository.register(existing);

    sut.update(
        new ExpenseAttributeUpdateCommand(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("交通費"),
            ExpectedDigest.of(existing.digest())));

    ExpenseAttribute updated =
        attributeRepository.find(new ExpenseAttributeIdentifier("attr-1")).orElseThrow();
    assertEquals("交通費", updated.expenseAttributeName().value());
    assertEquals(ExpenseCategory.変動費, updated.expenseCategory());
    assertNotEquals(
        existing.digest(), attributeRepository.digestOf(new ExpenseAttributeIdentifier("attr-1")));
  }

  @Test
  void 名前に変更がない場合は更新しない() {
    ExpenseAttribute existing =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    attributeRepository.register(existing);

    sut.update(
        new ExpenseAttributeUpdateCommand(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpectedDigest.of(existing.digest())));

    ExpenseAttribute result =
        attributeRepository.find(new ExpenseAttributeIdentifier("attr-1")).orElseThrow();
    assertSame(existing, result);
    assertEquals(
        existing.digest(), attributeRepository.digestOf(new ExpenseAttributeIdentifier("attr-1")));
  }

  @Test
  void 変更がなくてもバージョン不一致なら楽観ロック例外() {
    ExpenseAttribute existing =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    attributeRepository.register(existing);

    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.update(
                new ExpenseAttributeUpdateCommand(
                    new ExpenseAttributeIdentifier("attr-1"),
                    new ExpenseAttributeName("食費"),
                    ExpectedDigest.of(TestDigests.STALE))));
  }
}
