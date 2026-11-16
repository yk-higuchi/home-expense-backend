package dev.higuchi.homeexpense.query.income;

import dev.higuchi.homeexpense.core.model.income.IncomeIdentifier;
import dev.higuchi.homeexpense.core.model.income.IncomeNotFoundException;
import java.util.Optional;

public interface IncomeSearchResultQuerier {
  IncomeSearchResult search(IncomeSearchCriteria criteria);

  Optional<IncomeDetail> find(IncomeIdentifier id);

  default IncomeDetail get(IncomeIdentifier id) {
    return find(id).orElseThrow(IncomeNotFoundException::new);
  }
}
