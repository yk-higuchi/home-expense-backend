package dev.higuchi.homeexpense.query.expense;

import dev.higuchi.homeexpense.core.model.expense.ExpenseIdentifier;
import dev.higuchi.homeexpense.core.model.expense.ExpenseNotFoundException;
import java.util.Optional;

public interface ExpenseSearchResultQuerier {
  ExpenseSearchResult search(ExpenseSearchCriteria criteria);

  Optional<ExpenseDetail> find(ExpenseIdentifier id);

  default ExpenseDetail get(ExpenseIdentifier id) {
    return find(id).orElseThrow(ExpenseNotFoundException::new);
  }
}
