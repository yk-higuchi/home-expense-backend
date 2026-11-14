package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeNameUniqueness;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseAttributeUpdateService {

  ExpenseAttributeRepository expenseAttributeRepository;
  ExpenseAttributeNameUniqueness expenseAttributeNameUniqueness;

  public ExpenseAttributeUpdateService(ExpenseAttributeRepository expenseAttributeRepository) {
    this.expenseAttributeRepository = expenseAttributeRepository;
    this.expenseAttributeNameUniqueness =
        new ExpenseAttributeNameUniqueness(expenseAttributeRepository);
  }

  public void update(ExpenseAttributeUpdateCommand command) {
    ExpenseAttribute current = expenseAttributeRepository.get(command.expenseAttributeIdentifier());
    expenseAttributeNameUniqueness.assertUniqueForUpdate(current, command.expenseAttributeName());
    ExpenseAttribute updated = current.updateWith(command.expenseAttributeName());
    if (current.hasChanges(updated)) {
      expenseAttributeRepository.update(updated, command.expectedVersion());
    } else {
      expenseAttributeRepository.verifyVersion(current, command.expectedVersion());
    }
  }
}
