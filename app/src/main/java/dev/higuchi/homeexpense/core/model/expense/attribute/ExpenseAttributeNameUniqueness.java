package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;

public class ExpenseAttributeNameUniqueness {

  private final ExpenseAttributeNameLookup lookup;

  public ExpenseAttributeNameUniqueness(ExpenseAttributeNameLookup lookup) {
    this.lookup = lookup;
  }

  public void assertUniqueForRegistration(ExpenseAttributeName name, ExpenseCategory category) {
    if (lookup.existsByName(name, category)) {
      throw new ExpenseAttributeAlreadyExistsException();
    }
  }

  public void assertUniqueForUpdate(ExpenseAttribute current, ExpenseAttributeName newName) {
    if (current.hasSameName(newName)) {
      return;
    }
    if (lookup.existsByName(newName, current.expenseCategory())) {
      throw new ExpenseAttributeAlreadyExistsException();
    }
  }
}
