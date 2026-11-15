package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;

public class ExpenseAttributeLimitExceededException extends RuntimeException {
  private final ExpenseCategory category;

  public ExpenseAttributeLimitExceededException(ExpenseCategory category) {
    super(category.name() + "の経費属性は" + ExpenseAttributeList.MAX_SIZE + "件まで登録できます");
    this.category = category;
  }

  public ExpenseCategory category() {
    return category;
  }
}
