package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.income.*;
import dev.higuchi.homeexpense.core.model.income.InvalidIncomeAttributeReferenceException;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeUpdateService {

  IncomeRepository incomeRepository;
  IncomeAttributeRepository incomeAttributeRepository;

  public IncomeUpdateService(
      IncomeRepository incomeRepository, IncomeAttributeRepository incomeAttributeRepository) {
    this.incomeRepository = incomeRepository;
    this.incomeAttributeRepository = incomeAttributeRepository;
  }

  public void update(IncomeUpdateCommand command) {
    Income current = incomeRepository.get(command.incomeIdentifier());
    IncomeAttribute attribute =
        incomeAttributeRepository
            .find(command.incomeAttributeIdentifier())
            .orElseThrow(InvalidIncomeAttributeReferenceException::new);
    Income updated =
        current.updateWith(
            command.description(),
            command.amount(),
            command.receiveDate(),
            attribute.incomeAttributeIdentifier());
    if (current.hasChanges(updated)) {
      incomeRepository.update(updated, command.expectedDigest());
    } else {
      incomeRepository.verifyDigest(current, command.expectedDigest());
    }
  }
}
