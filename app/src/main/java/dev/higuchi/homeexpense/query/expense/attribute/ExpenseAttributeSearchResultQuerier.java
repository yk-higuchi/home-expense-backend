package dev.higuchi.homeexpense.query.expense.attribute;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeNotFoundException;
import java.util.List;
import java.util.Optional;

public interface ExpenseAttributeSearchResultQuerier {
  List<ExpenseAttributeDetail> list(Optional<ExpenseCategory> category);

  Optional<ExpenseAttributeDetail> find(ExpenseAttributeIdentifier id);

  default ExpenseAttributeDetail get(ExpenseAttributeIdentifier id) {
    return find(id).orElseThrow(ExpenseAttributeNotFoundException::new);
  }
}
