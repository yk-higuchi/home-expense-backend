package dev.higuchi.homeexpense.core.model.expense;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import java.util.Optional;

public interface ExpenseRepository {

  void register(Expense expense);

  Optional<Expense> find(ExpenseIdentifier expenseIdentifier);

  default Expense get(ExpenseIdentifier expenseIdentifier) {
    return find(expenseIdentifier).orElseThrow(ExpenseNotFoundException::new);
  }

  boolean existsByAttributeIdentifier(ExpenseAttributeIdentifier expenseAttributeIdentifier);

  void update(Expense expense, ExpectedVersion expectedVersion);

  void verifyVersion(Expense expense, ExpectedVersion expectedVersion);

  void delete(Expense expense);
}
