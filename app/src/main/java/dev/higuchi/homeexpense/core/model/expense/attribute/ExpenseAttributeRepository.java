package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import java.util.Optional;

public interface ExpenseAttributeRepository extends ExpenseAttributeNameLookup {

  void register(ExpenseAttribute expenseAttribute);

  Optional<ExpenseAttribute> find(ExpenseAttributeIdentifier expenseAttributeIdentifier);

  default ExpenseAttribute get(ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    return find(expenseAttributeIdentifier).orElseThrow(ExpenseAttributeNotFoundException::new);
  }

  void update(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest);

  void verifyDigest(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest);

  void delete(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest);

  ExpenseAttributeList list(ExpenseCategory category);

  void verifyList(ExpenseAttributeList current, ExpectedDigest expectedDigest);

  void reorder(ExpenseAttributeList list);
}
