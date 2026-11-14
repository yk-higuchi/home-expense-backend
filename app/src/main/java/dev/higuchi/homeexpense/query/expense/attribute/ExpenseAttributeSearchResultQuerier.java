package dev.higuchi.homeexpense.query.expense.attribute;

import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeNotFoundException;
import java.util.Optional;

public interface ExpenseAttributeSearchResultQuerier {
  ExpenseAttributeSearchResult search(ExpenseAttributeSearchCriteria criteria);

  Optional<ExpenseAttributeDetail> find(ExpenseAttributeIdentifier id);

  default ExpenseAttributeDetail get(ExpenseAttributeIdentifier id) {
    return find(id).orElseThrow(ExpenseAttributeNotFoundException::new);
  }
}
