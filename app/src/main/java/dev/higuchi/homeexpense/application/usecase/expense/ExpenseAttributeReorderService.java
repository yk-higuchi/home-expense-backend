package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeOrder;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeRepository;
import dev.higuchi.homeexpense.core.model.expense.attribute.InvalidExpenseAttributeOrderException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseAttributeReorderService {

  ExpenseAttributeRepository expenseAttributeRepository;

  public ExpenseAttributeReorderService(ExpenseAttributeRepository expenseAttributeRepository) {
    this.expenseAttributeRepository = expenseAttributeRepository;
  }

  public DocumentDigest reorder(ExpenseAttributeReorderCommand command) {
    ExpenseAttributeOrder requested = command.order();
    ExpenseAttributeOrder current = expenseAttributeRepository.order(requested.category());
    expenseAttributeRepository.verifyOrder(current, command.expectedDigest());
    if (!requested.hasSameMembersAs(current)) {
      throw new InvalidExpenseAttributeOrderException();
    }
    if (!requested.equals(current)) {
      expenseAttributeRepository.reorder(requested);
    }
    return requested.digest();
  }
}
