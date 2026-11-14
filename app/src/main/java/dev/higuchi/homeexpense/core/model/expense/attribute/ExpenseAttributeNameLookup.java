package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;

/** 経費属性名の存在確認 port */
@FunctionalInterface
public interface ExpenseAttributeNameLookup {
  boolean existsByName(ExpenseAttributeName expenseAttributeName, ExpenseCategory expenseCategory);
}
