package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeAttributeRegistrationService {

  IncomeAttributeRepository incomeAttributeRepository;
  IncomeAttributeNameUniqueness incomeAttributeNameUniqueness;

  public IncomeAttributeRegistrationService(IncomeAttributeRepository incomeAttributeRepository) {
    this.incomeAttributeRepository = incomeAttributeRepository;
    this.incomeAttributeNameUniqueness =
        new IncomeAttributeNameUniqueness(incomeAttributeRepository);
  }

  public IncomeAttributeIdentifier register(IncomeAttributeName incomeAttributeName) {
    if (incomeAttributeRepository.list().size() >= IncomeAttributeList.MAX_SIZE) {
      throw new IncomeAttributeLimitExceededException();
    }
    incomeAttributeNameUniqueness.assertUniqueForRegistration(incomeAttributeName);
    IncomeAttribute incomeAttribute = IncomeAttribute.create(incomeAttributeName);
    incomeAttributeRepository.register(incomeAttribute);
    return incomeAttribute.incomeAttributeIdentifier();
  }
}
