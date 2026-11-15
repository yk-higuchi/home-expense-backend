package dev.higuchi.homeexpense.core.model.income.attribute;

public class IncomeAttributeLimitExceededException extends RuntimeException {
  public IncomeAttributeLimitExceededException() {
    super("収入属性は" + IncomeAttributeList.MAX_SIZE + "件まで登録できます");
  }
}
