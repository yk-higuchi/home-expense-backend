package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseAttributeRegistrationService {

  ExpenseAttributeRepository expenseAttributeRepository;
  ExpenseAttributeNameUniqueness expenseAttributeNameUniqueness;

  public ExpenseAttributeRegistrationService(
      ExpenseAttributeRepository expenseAttributeRepository) {
    this.expenseAttributeRepository = expenseAttributeRepository;
    this.expenseAttributeNameUniqueness =
        new ExpenseAttributeNameUniqueness(expenseAttributeRepository);
  }

  public ExpenseAttributeIdentifier register(ExpenseAttributeRegistrationCommand command) {
    if (expenseAttributeRepository.list(command.expenseCategory()).size()
        >= ExpenseAttributeList.MAX_SIZE) {
      throw new ExpenseAttributeLimitExceededException(command.expenseCategory());
    }
    expenseAttributeNameUniqueness.assertUniqueForRegistration(
        command.expenseAttributeName(), command.expenseCategory());
    ExpenseAttribute expenseAttribute =
        ExpenseAttribute.create(command.expenseAttributeName(), command.expenseCategory());
    expenseAttributeRepository.register(expenseAttribute);
    return expenseAttribute.expenseAttributeIdentifier();
  }
}
