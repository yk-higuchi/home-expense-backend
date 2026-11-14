package dev.higuchi.homeexpense.core.model.expense;

/** 経費が見つからない */
public class ExpenseNotFoundException extends RuntimeException {
  public ExpenseNotFoundException() {
    super();
  }
}
