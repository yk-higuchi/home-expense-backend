package dev.higuchi.homeexpense.core.model.income;

/** 収入が参照する収入属性が存在しない（参照先の不在であり、操作対象リソースの不在ではない） */
public class InvalidIncomeAttributeReferenceException extends RuntimeException {
  public InvalidIncomeAttributeReferenceException() {
    super();
  }
}
