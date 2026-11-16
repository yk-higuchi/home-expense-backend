package dev.higuchi.homeexpense.application.usecase.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.AttributeMove;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeName;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeAttributeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncomeAttributeListPatchServiceTest {

  InMemoryIncomeAttributeRepository repository = new InMemoryIncomeAttributeRepository();
  IncomeAttributeListPatchService sut = new IncomeAttributeListPatchService(repository);

  IncomeAttributeIdentifier a = new IncomeAttributeIdentifier("a");
  IncomeAttributeIdentifier b = new IncomeAttributeIdentifier("b");

  @BeforeEach
  void setUp() {
    repository.register(new IncomeAttribute(a, new IncomeAttributeName("給与")));
    repository.register(new IncomeAttribute(b, new IncomeAttributeName("賞与")));
  }

  @Test
  void 移動を適用して新しい一覧のダイジェストを返す() {
    var digest =
        sut.move(List.of(new AttributeMove(1, 0)), ExpectedDigest.of(repository.list().digest()));

    assertEquals(List.of(b, a), repository.list().identifiers());
    assertEquals(repository.list().digest(), digest);
  }

  @Test
  void 一覧のダイジェストが一致しなければ楽観ロック例外() {
    assertThrows(
        ConcurrentUpdateException.class,
        () -> sut.move(List.of(new AttributeMove(1, 0)), ExpectedDigest.of(TestDigests.STALE)));
  }
}
