package dev.higuchi.homeexpense.core.model.income.attribute;

public class IncomeAttributeNameUniqueness {

  private final IncomeAttributeNameLookup lookup;

  public IncomeAttributeNameUniqueness(IncomeAttributeNameLookup lookup) {
    this.lookup = lookup;
  }

  public void assertUniqueForRegistration(IncomeAttributeName name) {
    if (lookup.existsByName(name)) {
      throw new IncomeAttributeAlreadyExistsException();
    }
  }

  public void assertUniqueForUpdate(IncomeAttribute current, IncomeAttributeName newName) {
    if (current.hasSameName(newName)) {
      return;
    }
    if (lookup.existsByName(newName)) {
      throw new IncomeAttributeAlreadyExistsException();
    }
  }
}
