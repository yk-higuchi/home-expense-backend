package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.expense.*;
import dev.higuchi.homeexpense.core.model.expense.InvalidExpenseAttributeReferenceException;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseUpdateService {

  ExpenseRepository expenseRepository;
  ExpenseAttributeRepository expenseAttributeRepository;

  public ExpenseUpdateService(
      ExpenseRepository expenseRepository, ExpenseAttributeRepository expenseAttributeRepository) {
    this.expenseRepository = expenseRepository;
    this.expenseAttributeRepository = expenseAttributeRepository;
  }

  public void update(ExpenseUpdateCommand command) {
    Expense current = expenseRepository.get(command.expenseIdentifier());
    ExpenseAttribute attribute =
        expenseAttributeRepository
            .find(command.expenseAttributeIdentifier())
            .orElseThrow(InvalidExpenseAttributeReferenceException::new);
    Expense updated =
        current.updateWith(
            command.description(),
            command.amount(),
            command.paymentDate(),
            attribute.expenseAttributeIdentifier());
    if (current.hasChanges(updated)) {
      expenseRepository.update(updated, command.expectedVersion());
    } else {
      expenseRepository.verifyVersion(current, command.expectedVersion());
    }
  }
}
