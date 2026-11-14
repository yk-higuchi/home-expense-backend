package dev.higuchi.homeexpense.core.model.income.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import java.util.Optional;

/** 収入属性リポジトリ */
public interface IncomeAttributeRepository extends IncomeAttributeNameLookup {

  void register(IncomeAttribute incomeAttribute);

  Optional<IncomeAttribute> find(IncomeAttributeIdentifier incomeAttributeIdentifier);

  default IncomeAttribute get(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    return find(incomeAttributeIdentifier).orElseThrow(IncomeAttributeNotFoundException::new);
  }

  void update(IncomeAttribute incomeAttribute, ExpectedVersion expectedVersion);

  /** 書き込みを伴わずにバージョン条件を検証する。一致しなければ ConcurrentUpdateException */
  void verifyVersion(IncomeAttribute incomeAttribute, ExpectedVersion expectedVersion);

  void delete(IncomeAttribute incomeAttribute);
}
