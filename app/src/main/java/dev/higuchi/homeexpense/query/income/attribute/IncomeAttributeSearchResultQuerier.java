package dev.higuchi.homeexpense.query.income.attribute;

import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeNotFoundException;
import java.util.Optional;

public interface IncomeAttributeSearchResultQuerier {
  IncomeAttributeSearchResult search(IncomeAttributeSearchCriteria criteria);

  Optional<IncomeAttributeDetail> find(IncomeAttributeIdentifier id);

  default IncomeAttributeDetail get(IncomeAttributeIdentifier id) {
    return find(id).orElseThrow(IncomeAttributeNotFoundException::new);
  }
}
