package dev.higuchi.homeexpense.application.usecase.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.fake.InMemoryIncomeAttributeRepository;
import org.junit.jupiter.api.Test;

class IncomeAttributeRegistrationServiceTest {

  InMemoryIncomeAttributeRepository attributeRepository = new InMemoryIncomeAttributeRepository();
  IncomeAttributeRegistrationService sut =
      new IncomeAttributeRegistrationService(attributeRepository);

  @Test
  void 収入属性を作成して登録する() {
    IncomeAttributeIdentifier result = sut.register(new IncomeAttributeName("給与"));

    assertNotNull(result);
    assertEquals(1, attributeRepository.all().size());
    IncomeAttribute registered = attributeRepository.all().getFirst();
    assertEquals("給与", registered.incomeAttributeName().value());
  }

  @Test
  void 同名の収入属性が存在する場合は例外をスローする() {
    IncomeAttribute existing =
        new IncomeAttribute(new IncomeAttributeIdentifier("attr-1"), new IncomeAttributeName("給与"));
    attributeRepository.register(existing);

    assertThrows(
        IncomeAttributeAlreadyExistsException.class,
        () -> sut.register(new IncomeAttributeName("給与")));
  }

  @Test
  void 上限の件数まで登録でき_それを超えると例外() {
    for (int i = 0; i < IncomeAttributeList.MAX_SIZE; i++) {
      sut.register(new IncomeAttributeName("収入" + i));
    }

    IncomeAttributeLimitExceededException e =
        assertThrows(
            IncomeAttributeLimitExceededException.class,
            () -> sut.register(new IncomeAttributeName("超過")));
    assertEquals("収入属性は100件まで登録できます", e.getMessage());
  }
}
