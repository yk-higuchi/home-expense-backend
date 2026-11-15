package dev.higuchi.homeexpense.application.usecase.expense;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.AttributeMove;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.InvalidAttributeMoveException;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseAttributeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseAttributeListPatchServiceTest {

  InMemoryExpenseAttributeRepository repository = new InMemoryExpenseAttributeRepository();
  ExpenseAttributeListPatchService sut = new ExpenseAttributeListPatchService(repository);

  ExpenseAttributeIdentifier a = new ExpenseAttributeIdentifier("a");
  ExpenseAttributeIdentifier b = new ExpenseAttributeIdentifier("b");
  ExpenseAttributeIdentifier c = new ExpenseAttributeIdentifier("c");
  ExpenseAttributeIdentifier variable = new ExpenseAttributeIdentifier("v");

  @BeforeEach
  void setUp() {
    repository.register(attribute(a, "住居", ExpenseCategory.固定費));
    repository.register(attribute(b, "通信", ExpenseCategory.固定費));
    repository.register(attribute(variable, "食費", ExpenseCategory.変動費));
    repository.register(attribute(c, "保険", ExpenseCategory.固定費));
  }

  private static ExpenseAttribute attribute(
      ExpenseAttributeIdentifier id, String name, ExpenseCategory category) {
    return new ExpenseAttribute(id, new ExpenseAttributeName(name), category);
  }

  private ExpectedDigest current() {
    return ExpectedDigest.of(repository.list(ExpenseCategory.固定費).digest());
  }

  private List<ExpenseAttributeIdentifier> fixedOrder() {
    return repository.list(ExpenseCategory.固定費).identifiers();
  }

  @Test
  void 移動を適用して新しい一覧のダイジェストを返す() {
    var digest = sut.move(ExpenseCategory.固定費, List.of(new AttributeMove(2, 0)), current());

    assertEquals(List.of(c, a, b), fixedOrder());
    assertEquals(repository.list(ExpenseCategory.固定費).digest(), digest);
    assertEquals(List.of(variable), repository.list(ExpenseCategory.変動費).identifiers());
  }

  @Test
  void 複数の移動を順に適用する() {
    sut.move(
        ExpenseCategory.固定費,
        List.of(new AttributeMove(0, AttributeMove.END), new AttributeMove(0, 1)),
        current());

    assertEquals(List.of(c, b, a), fixedOrder());
  }

  @Test
  void 一覧のダイジェストが一致しなければ楽観ロック例外() {
    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.move(
                ExpenseCategory.固定費,
                List.of(new AttributeMove(2, 0)),
                ExpectedDigest.of(TestDigests.STALE)));
    assertEquals(List.of(a, b, c), fixedOrder());
  }

  @Test
  void 範囲外の位置なら例外で_一部だけ適用されることもない() {
    assertThrows(
        InvalidAttributeMoveException.class,
        () ->
            sut.move(
                ExpenseCategory.固定費,
                List.of(new AttributeMove(2, 0), new AttributeMove(3, 0)),
                current()));
    assertEquals(List.of(a, b, c), fixedOrder());
  }

  @Test
  void 移動しても並びが変わらなければ同じダイジェストを返す() {
    var before = repository.list(ExpenseCategory.固定費).digest();
    assertEquals(
        before, sut.move(ExpenseCategory.固定費, List.of(new AttributeMove(1, 1)), current()));
  }
}
