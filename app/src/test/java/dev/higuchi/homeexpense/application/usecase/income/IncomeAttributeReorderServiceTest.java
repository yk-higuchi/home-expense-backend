package dev.higuchi.homeexpense.application.usecase.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeName;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeOrder;
import dev.higuchi.homeexpense.core.model.income.attribute.InvalidIncomeAttributeOrderException;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeAttributeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncomeAttributeReorderServiceTest {

  InMemoryIncomeAttributeRepository repository = new InMemoryIncomeAttributeRepository();
  IncomeAttributeReorderService sut = new IncomeAttributeReorderService(repository);

  IncomeAttributeIdentifier a = new IncomeAttributeIdentifier("a");
  IncomeAttributeIdentifier b = new IncomeAttributeIdentifier("b");

  @BeforeEach
  void setUp() {
    repository.register(new IncomeAttribute(a, new IncomeAttributeName("給与")));
    repository.register(new IncomeAttribute(b, new IncomeAttributeName("賞与")));
  }

  @Test
  void 並びを置き換えて新しい並びのダイジェストを返す() {
    var expected = ExpectedDigest.of(repository.order().digest());
    var digest =
        sut.reorder(
            new IncomeAttributeReorderCommand(new IncomeAttributeOrder(List.of(b, a)), expected));

    assertEquals(List.of(b, a), repository.order().identifiers());
    assertEquals(new IncomeAttributeOrder(List.of(b, a)).digest(), digest);
  }

  @Test
  void 並びのダイジェストが一致しなければ楽観ロック例外() {
    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.reorder(
                new IncomeAttributeReorderCommand(
                    new IncomeAttributeOrder(List.of(b, a)),
                    ExpectedDigest.of(TestDigests.STALE))));
  }

  @Test
  void 今の属性と過不足があれば例外() {
    var expected = ExpectedDigest.of(repository.order().digest());
    assertThrows(
        InvalidIncomeAttributeOrderException.class,
        () ->
            sut.reorder(
                new IncomeAttributeReorderCommand(new IncomeAttributeOrder(List.of(a)), expected)));
  }
}
