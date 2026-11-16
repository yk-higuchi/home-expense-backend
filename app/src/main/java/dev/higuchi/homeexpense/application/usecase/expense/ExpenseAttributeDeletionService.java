package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseRepository;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseAttributeDeletionService {

  ExpenseAttributeRepository expenseAttributeRepository;
  ExpenseRepository expenseRepository;

  public ExpenseAttributeDeletionService(
      ExpenseAttributeRepository expenseAttributeRepository, ExpenseRepository expenseRepository) {
    this.expenseAttributeRepository = expenseAttributeRepository;
    this.expenseRepository = expenseRepository;
  }

  public void delete(
      ExpenseAttributeIdentifier expenseAttributeIdentifier, ExpectedDigest expectedDigest) {
    ExpenseAttribute attribute = expenseAttributeRepository.get(expenseAttributeIdentifier);
    if (expenseRepository.existsByAttributeIdentifier(attribute.expenseAttributeIdentifier())) {
      throw new ExpenseAttributeConstraintException();
    }
    expenseAttributeRepository.delete(attribute, expectedDigest);
  }
}
