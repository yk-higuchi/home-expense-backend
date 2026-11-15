package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.Expense;
import dev.higuchi.homeexpense.core.model.expense.ExpenseIdentifier;
import dev.higuchi.homeexpense.core.model.expense.ExpenseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseDeletionService {

  ExpenseRepository expenseRepository;

  public ExpenseDeletionService(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  public void delete(ExpenseIdentifier expenseIdentifier, ExpectedVersion expectedVersion) {
    Expense expense = expenseRepository.get(expenseIdentifier);
    expenseRepository.delete(expense, expectedVersion);
  }
}
