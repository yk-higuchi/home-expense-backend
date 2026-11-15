package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import java.util.Optional;

public interface ExpenseAttributeRepository extends ExpenseAttributeNameLookup {

  void register(ExpenseAttribute expenseAttribute);

  Optional<ExpenseAttribute> find(ExpenseAttributeIdentifier expenseAttributeIdentifier);

  default ExpenseAttribute get(ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    return find(expenseAttributeIdentifier).orElseThrow(ExpenseAttributeNotFoundException::new);
  }

  void update(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion);

  void verifyVersion(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion);

  void delete(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion);
}
