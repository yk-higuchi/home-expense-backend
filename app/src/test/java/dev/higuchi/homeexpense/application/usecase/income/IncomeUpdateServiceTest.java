package dev.higuchi.homeexpense.application.usecase.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.*;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeAttributeRepository;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class IncomeUpdateServiceTest {

  InMemoryIncomeRepository incomeRepository = new InMemoryIncomeRepository();
  InMemoryIncomeAttributeRepository attributeRepository = new InMemoryIncomeAttributeRepository();
  IncomeUpdateService sut = new IncomeUpdateService(incomeRepository, attributeRepository);

  IncomeAttribute attribute =
      new IncomeAttribute(new IncomeAttributeIdentifier("attr-1"), new IncomeAttributeName("給与"));

  @Test
  void 収入を更新する() {
    Income existing =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    incomeRepository.register(existing);
    attributeRepository.register(attribute);

    sut.update(
        new IncomeUpdateCommand(
            new IncomeIdentifier("inc-1"),
            new Description("5月給与"),
            new Amount(310000),
            new ReceiveDate(LocalDate.of(2026, 5, 25)),
            new IncomeAttributeIdentifier("attr-1"),
            ExpectedDigest.of(existing.digest())));

    Income updated = incomeRepository.find(new IncomeIdentifier("inc-1")).orElseThrow();
    assertEquals("5月給与", updated.description().value());
    assertEquals(310000, updated.amount().value());
    assertNotEquals(existing.digest(), incomeRepository.digestOf(new IncomeIdentifier("inc-1")));
  }

  @Test
  void 変更がない場合は更新しない() {
    Income existing =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    incomeRepository.register(existing);
    attributeRepository.register(attribute);

    sut.update(
        new IncomeUpdateCommand(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"),
            ExpectedDigest.of(existing.digest())));

    Income result = incomeRepository.find(new IncomeIdentifier("inc-1")).orElseThrow();
    assertSame(existing, result);
    assertEquals(existing.digest(), incomeRepository.digestOf(new IncomeIdentifier("inc-1")));
  }

  @Test
  void 存在しない収入を更新すると例外をスローする() {
    attributeRepository.register(attribute);

    assertThrows(
        IncomeNotFoundException.class,
        () ->
            sut.update(
                new IncomeUpdateCommand(
                    new IncomeIdentifier("not-exist"),
                    new Description("4月給与"),
                    new Amount(300000),
                    new ReceiveDate(LocalDate.of(2026, 4, 25)),
                    new IncomeAttributeIdentifier("attr-1"),
                    ExpectedDigest.of(TestDigests.STALE))));
  }

  @Test
  void 存在しない属性IDで更新すると例外をスローする() {
    Income existing =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    incomeRepository.register(existing);

    assertThrows(
        InvalidIncomeAttributeReferenceException.class,
        () ->
            sut.update(
                new IncomeUpdateCommand(
                    new IncomeIdentifier("inc-1"),
                    new Description("5月給与"),
                    new Amount(310000),
                    new ReceiveDate(LocalDate.of(2026, 5, 25)),
                    new IncomeAttributeIdentifier("not-exist"),
                    ExpectedDigest.of(existing.digest()))));
  }

  @Test
  void 変更がなくてもバージョン不一致なら楽観ロック例外() {
    Income existing =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    incomeRepository.register(existing);
    attributeRepository.register(attribute);

    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.update(
                new IncomeUpdateCommand(
                    new IncomeIdentifier("inc-1"),
                    new Description("4月給与"),
                    new Amount(300000),
                    new ReceiveDate(LocalDate.of(2026, 4, 25)),
                    new IncomeAttributeIdentifier("attr-1"),
                    ExpectedDigest.of(TestDigests.STALE))));
  }
}
