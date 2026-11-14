package dev.yhiguchi.home_expense.domain.model.income;

import dev.yhiguchi.home_expense.domain.model.ExpectedVersion;
import dev.yhiguchi.home_expense.domain.model.income.attribute.IncomeAttributeIdentifier;
import java.util.Optional;

/** 収入リポジトリ */
public interface IncomeRepository {

  void register(Income income);

  Optional<Income> find(IncomeIdentifier incomeIdentifier);

  default Income get(IncomeIdentifier incomeIdentifier) {
    return find(incomeIdentifier).orElseThrow(IncomeNotFoundException::new);
  }

  boolean existsByAttributeIdentifier(IncomeAttributeIdentifier incomeAttributeIdentifier);

  void update(Income income, ExpectedVersion expectedVersion);

  /** 書き込みを伴わずにバージョン条件を検証する。一致しなければ ConcurrentUpdateException */
  void verifyVersion(Income income, ExpectedVersion expectedVersion);

  void delete(Income income);
}
