package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeNameUniqueness;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeAttributeUpdateService {

  IncomeAttributeRepository incomeAttributeRepository;
  IncomeAttributeNameUniqueness incomeAttributeNameUniqueness;

  public IncomeAttributeUpdateService(IncomeAttributeRepository incomeAttributeRepository) {
    this.incomeAttributeRepository = incomeAttributeRepository;
    this.incomeAttributeNameUniqueness =
        new IncomeAttributeNameUniqueness(incomeAttributeRepository);
  }

  public void update(IncomeAttributeUpdateCommand command) {
    IncomeAttribute current = incomeAttributeRepository.get(command.incomeAttributeIdentifier());
    incomeAttributeNameUniqueness.assertUniqueForUpdate(current, command.incomeAttributeName());
    IncomeAttribute updated = current.updateWith(command.incomeAttributeName());
    if (current.hasChanges(updated)) {
      incomeAttributeRepository.update(updated, command.expectedVersion());
    } else {
      incomeAttributeRepository.verifyVersion(current, command.expectedVersion());
    }
  }
}
