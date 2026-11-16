package dev.higuchi.homeexpense.core.model.income;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import java.util.Optional;

public interface IncomeRepository {

  void register(Income income);

  Optional<Income> find(IncomeIdentifier incomeIdentifier);

  default Income get(IncomeIdentifier incomeIdentifier) {
    return find(incomeIdentifier).orElseThrow(IncomeNotFoundException::new);
  }

  boolean existsByAttributeIdentifier(IncomeAttributeIdentifier incomeAttributeIdentifier);

  void update(Income income, ExpectedDigest expectedDigest);

  void verifyDigest(Income income, ExpectedDigest expectedDigest);

  void delete(Income income, ExpectedDigest expectedDigest);
}
