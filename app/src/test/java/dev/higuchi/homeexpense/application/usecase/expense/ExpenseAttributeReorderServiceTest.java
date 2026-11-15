package dev.higuchi.homeexpense.application.usecase.expense;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeOrder;
import dev.higuchi.homeexpense.core.model.expense.attribute.InvalidExpenseAttributeOrderException;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseAttributeRepository;
import dev.higuchi.homeexpense.testing.TestDigests;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExpenseAttributeReorderServiceTest {

  InMemoryExpenseAttributeRepository repository = new InMemoryExpenseAttributeRepository();
  ExpenseAttributeReorderService sut = new ExpenseAttributeReorderService(repository);

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

  private ExpenseAttributeOrder fixed(ExpenseAttributeIdentifier... ids) {
    return new ExpenseAttributeOrder(ExpenseCategory.固定費, List.of(ids));
  }

  private ExpectedDigest current() {
    return ExpectedDigest.of(repository.order(ExpenseCategory.固定費).digest());
  }

  @Test
  void 並びを置き換えて新しい並びのダイジェストを返す() {
    var digest = sut.reorder(new ExpenseAttributeReorderCommand(fixed(c, a, b), current()));

    assertEquals(fixed(c, a, b), repository.order(ExpenseCategory.固定費));
    assertEquals(fixed(c, a, b).digest(), digest);
    assertEquals(List.of(variable), repository.order(ExpenseCategory.変動費).identifiers());
  }

  @Test
  void 並びのダイジェストが一致しなければ楽観ロック例外() {
    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.reorder(
                new ExpenseAttributeReorderCommand(
                    fixed(c, a, b), ExpectedDigest.of(TestDigests.STALE))));
    assertEquals(fixed(a, b, c), repository.order(ExpenseCategory.固定費));
  }

  @Test
  void 今の属性と過不足があれば例外() {
    assertThrows(
        InvalidExpenseAttributeOrderException.class,
        () -> sut.reorder(new ExpenseAttributeReorderCommand(fixed(a, b), current())));
    assertThrows(
        InvalidExpenseAttributeOrderException.class,
        () -> sut.reorder(new ExpenseAttributeReorderCommand(fixed(a, b, c, variable), current())));
    assertEquals(fixed(a, b, c), repository.order(ExpenseCategory.固定費));
  }

  @Test
  void ダイジェストが古く過不足もある場合は楽観ロック例外を優先する() {
    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.reorder(
                new ExpenseAttributeReorderCommand(
                    fixed(a, b), ExpectedDigest.of(TestDigests.STALE))));
  }

  @Test
  void 条件なしの指定でも過不足は検査する() {
    assertThrows(
        InvalidExpenseAttributeOrderException.class,
        () ->
            sut.reorder(
                new ExpenseAttributeReorderCommand(fixed(a, b), ExpectedDigest.unconditional())));
    sut.reorder(new ExpenseAttributeReorderCommand(fixed(b, c, a), ExpectedDigest.unconditional()));
    assertEquals(fixed(b, c, a), repository.order(ExpenseCategory.固定費));
  }
}
