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
public class ExpenseRegistrationService {

  ExpenseRepository expenseRepository;
  ExpenseAttributeRepository expenseAttributeRepository;

  public ExpenseRegistrationService(
      ExpenseRepository expenseRepository, ExpenseAttributeRepository expenseAttributeRepository) {
    this.expenseRepository = expenseRepository;
    this.expenseAttributeRepository = expenseAttributeRepository;
  }

  public ExpenseIdentifier register(ExpenseRegistrationCommand command) {
    ExpenseAttribute attribute =
        expenseAttributeRepository
            .find(command.expenseAttributeIdentifier())
            .orElseThrow(InvalidExpenseAttributeReferenceException::new);
    Expense expense =
        Expense.create(
            command.description(),
            command.amount(),
            command.paymentDate(),
            attribute.expenseAttributeIdentifier());
    expenseRepository.register(expense);
    return expense.expenseIdentifier();
  }
}
