package dev.higuchi.homeexpense.core.model.income.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import java.util.Optional;

public interface IncomeAttributeRepository extends IncomeAttributeNameLookup {

  void register(IncomeAttribute incomeAttribute);

  Optional<IncomeAttribute> find(IncomeAttributeIdentifier incomeAttributeIdentifier);

  default IncomeAttribute get(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    return find(incomeAttributeIdentifier).orElseThrow(IncomeAttributeNotFoundException::new);
  }

  void update(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest);

  void verifyDigest(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest);

  void delete(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest);
}
