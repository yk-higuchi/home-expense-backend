package dev.higuchi.homeexpense.core.model.expense;

/** 経費が参照する経費属性が存在しない（参照先の不在であり、操作対象リソースの不在ではない） */
public class InvalidExpenseAttributeReferenceException extends RuntimeException {
  public InvalidExpenseAttributeReferenceException() {
    super();
  }
}
