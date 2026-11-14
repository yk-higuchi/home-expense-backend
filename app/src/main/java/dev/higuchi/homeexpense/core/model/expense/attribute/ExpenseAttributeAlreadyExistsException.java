package dev.higuchi.homeexpense.core.model.expense.attribute;

/** 経費属性が既に存在する */
public class ExpenseAttributeAlreadyExistsException extends RuntimeException {
  public ExpenseAttributeAlreadyExistsException() {
    super();
  }
}
