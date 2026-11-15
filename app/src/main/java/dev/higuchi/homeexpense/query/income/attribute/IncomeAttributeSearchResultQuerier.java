package dev.higuchi.homeexpense.query.income.attribute;

import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeNotFoundException;
import java.util.List;
import java.util.Optional;

public interface IncomeAttributeSearchResultQuerier {
  List<IncomeAttributeDetail> list();

  Optional<IncomeAttributeDetail> find(IncomeAttributeIdentifier id);

  default IncomeAttributeDetail get(IncomeAttributeIdentifier id) {
    return find(id).orElseThrow(IncomeAttributeNotFoundException::new);
  }
}
