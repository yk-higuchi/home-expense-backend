package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import java.util.Optional;

/** 経費属性リポジトリ */
public interface ExpenseAttributeRepository extends ExpenseAttributeNameLookup {

  void register(ExpenseAttribute expenseAttribute);

  Optional<ExpenseAttribute> find(ExpenseAttributeIdentifier expenseAttributeIdentifier);

  default ExpenseAttribute get(ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    return find(expenseAttributeIdentifier).orElseThrow(ExpenseAttributeNotFoundException::new);
  }

  void update(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion);

  /** 書き込みを伴わずにバージョン条件を検証する。一致しなければ ConcurrentUpdateException */
  void verifyVersion(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion);

  void delete(ExpenseAttribute expenseAttribute);
}
