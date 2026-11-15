package dev.higuchi.homeexpense.core.model.income;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import java.util.Optional;

public interface IncomeRepository {

  void register(Income income);

  Optional<Income> find(IncomeIdentifier incomeIdentifier);

  default Income get(IncomeIdentifier incomeIdentifier) {
    return find(incomeIdentifier).orElseThrow(IncomeNotFoundException::new);
  }

  boolean existsByAttributeIdentifier(IncomeAttributeIdentifier incomeAttributeIdentifier);

  void update(Income income, ExpectedVersion expectedVersion);

  void verifyVersion(Income income, ExpectedVersion expectedVersion);

  void delete(Income income);
}
