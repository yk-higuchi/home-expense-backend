package dev.higuchi.homeexpense.core.model.income.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public record IncomeAttributeOrder(List<IncomeAttributeIdentifier> identifiers) {

  public IncomeAttributeOrder {
    identifiers = List.copyOf(identifiers);
  }

  public int size() {
    return identifiers.size();
  }

  public boolean hasSameMembersAs(IncomeAttributeOrder other) {
    return identifiers.size() == other.identifiers.size()
        && new HashSet<>(identifiers).size() == identifiers.size()
        && new HashSet<>(identifiers).equals(new HashSet<>(other.identifiers));
  }

  public DocumentDigest digest() {
    return DocumentDigest.of(
        Map.of("ids", identifiers.stream().map(IncomeAttributeIdentifier::value).toList()));
  }
}
