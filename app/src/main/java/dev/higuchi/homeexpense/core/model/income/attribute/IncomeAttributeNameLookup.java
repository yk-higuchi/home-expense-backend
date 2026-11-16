package dev.higuchi.homeexpense.core.model.income.attribute;

@FunctionalInterface
public interface IncomeAttributeNameLookup {
  boolean existsByName(IncomeAttributeName incomeAttributeName);
}
