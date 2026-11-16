package dev.higuchi.homeexpense.application.usecase.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeAttributeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import org.junit.jupiter.api.Test;

class IncomeAttributeUpdateServiceTest {

  InMemoryIncomeAttributeRepository attributeRepository = new InMemoryIncomeAttributeRepository();
  IncomeAttributeUpdateService sut = new IncomeAttributeUpdateService(attributeRepository);

  @Test
  void 収入属性を更新する() {
    IncomeAttribute existing =
        new IncomeAttribute(new IncomeAttributeIdentifier("attr-1"), new IncomeAttributeName("給与"));
    attributeRepository.register(existing);

    sut.update(
        new IncomeAttributeUpdateCommand(
            new IncomeAttributeIdentifier("attr-1"),
            new IncomeAttributeName("賞与"),
            ExpectedDigest.of(existing.digest())));

    IncomeAttribute updated =
        attributeRepository.find(new IncomeAttributeIdentifier("attr-1")).orElseThrow();
    assertEquals("賞与", updated.incomeAttributeName().value());
    assertNotEquals(
        existing.digest(), attributeRepository.digestOf(new IncomeAttributeIdentifier("attr-1")));
  }

  @Test
  void 変更がない場合は更新しない() {
    IncomeAttribute existing =
        new IncomeAttribute(new IncomeAttributeIdentifier("attr-1"), new IncomeAttributeName("給与"));
    attributeRepository.register(existing);

    sut.update(
        new IncomeAttributeUpdateCommand(
            new IncomeAttributeIdentifier("attr-1"),
            new IncomeAttributeName("給与"),
            ExpectedDigest.of(existing.digest())));

    IncomeAttribute result =
        attributeRepository.find(new IncomeAttributeIdentifier("attr-1")).orElseThrow();
    assertSame(existing, result);
    assertEquals(
        existing.digest(), attributeRepository.digestOf(new IncomeAttributeIdentifier("attr-1")));
  }

  @Test
  void 変更がなくてもバージョン不一致なら楽観ロック例外() {
    IncomeAttribute existing =
        new IncomeAttribute(new IncomeAttributeIdentifier("attr-1"), new IncomeAttributeName("給与"));
    attributeRepository.register(existing);

    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.update(
                new IncomeAttributeUpdateCommand(
                    new IncomeAttributeIdentifier("attr-1"),
                    new IncomeAttributeName("給与"),
                    ExpectedDigest.of(TestDigests.STALE))));
  }
}
