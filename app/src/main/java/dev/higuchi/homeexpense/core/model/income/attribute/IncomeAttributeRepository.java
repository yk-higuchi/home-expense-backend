package dev.higuchi.homeexpense.core.model.income.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import java.util.Optional;

public interface IncomeAttributeRepository extends IncomeAttributeNameLookup {

  void register(IncomeAttribute incomeAttribute);

  Optional<IncomeAttribute> find(IncomeAttributeIdentifier incomeAttributeIdentifier);

  default IncomeAttribute get(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    return find(incomeAttributeIdentifier).orElseThrow(IncomeAttributeNotFoundException::new);
  }

  void update(IncomeAttribute incomeAttribute, ExpectedVersion expectedVersion);

  void verifyVersion(IncomeAttribute incomeAttribute, ExpectedVersion expectedVersion);

  void delete(IncomeAttribute incomeAttribute, ExpectedVersion expectedVersion);
}
