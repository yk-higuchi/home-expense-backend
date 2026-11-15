package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;

@FunctionalInterface
public interface ExpenseAttributeNameLookup {
  boolean existsByName(ExpenseAttributeName expenseAttributeName, ExpenseCategory expenseCategory);
}
