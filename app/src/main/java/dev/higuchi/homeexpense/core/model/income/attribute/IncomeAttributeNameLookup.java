package dev.higuchi.homeexpense.core.model.income.attribute;

/** 収入属性名の存在確認 port */
@FunctionalInterface
public interface IncomeAttributeNameLookup {
  boolean existsByName(IncomeAttributeName incomeAttributeName);
}
