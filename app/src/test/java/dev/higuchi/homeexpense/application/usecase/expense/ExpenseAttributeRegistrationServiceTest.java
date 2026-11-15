package dev.higuchi.homeexpense.application.usecase.expense;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryExpenseAttributeRepository;
import org.junit.jupiter.api.Test;

class ExpenseAttributeRegistrationServiceTest {

  InMemoryExpenseAttributeRepository attributeRepository = new InMemoryExpenseAttributeRepository();
  ExpenseAttributeRegistrationService sut =
      new ExpenseAttributeRegistrationService(attributeRepository);

  @Test
  void 経費属性を作成して登録する() {
    ExpenseAttributeIdentifier result =
        sut.register(
            new ExpenseAttributeRegistrationCommand(
                new ExpenseAttributeName("食費"), ExpenseCategory.変動費));

    assertNotNull(result);
    assertEquals(1, attributeRepository.all().size());
    ExpenseAttribute registered = attributeRepository.all().getFirst();
    assertEquals("食費", registered.expenseAttributeName().value());
    assertEquals(ExpenseCategory.変動費, registered.expenseCategory());
  }

  @Test
  void 同名の経費属性が存在する場合は例外をスローする() {
    ExpenseAttribute existing =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    attributeRepository.register(existing);

    assertThrows(
        ExpenseAttributeAlreadyExistsException.class,
        () ->
            sut.register(
                new ExpenseAttributeRegistrationCommand(
                    new ExpenseAttributeName("食費"), ExpenseCategory.変動費)));
  }

  @Test
  void 分類ごとに上限の件数まで登録でき_それを超えると例外() {
    for (int i = 0; i < ExpenseAttributeList.MAX_SIZE; i++) {
      sut.register(
          new ExpenseAttributeRegistrationCommand(
              new ExpenseAttributeName("固定" + i), ExpenseCategory.固定費));
    }

    ExpenseAttributeLimitExceededException e =
        assertThrows(
            ExpenseAttributeLimitExceededException.class,
            () ->
                sut.register(
                    new ExpenseAttributeRegistrationCommand(
                        new ExpenseAttributeName("超過"), ExpenseCategory.固定費)));
    assertEquals("固定費の経費属性は100件まで登録できます", e.getMessage());
    assertDoesNotThrow(() ->
        sut.register(
            new ExpenseAttributeRegistrationCommand(
                new ExpenseAttributeName("変動"), ExpenseCategory.変動費)));
  }
}
