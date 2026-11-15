package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ExpenseAttributeOrder(
    ExpenseCategory category, List<ExpenseAttributeIdentifier> identifiers) {

  public ExpenseAttributeOrder {
    Objects.requireNonNull(category, "分類は必須です");
    identifiers = List.copyOf(identifiers);
  }

  public int size() {
    return identifiers.size();
  }

  public boolean hasSameMembersAs(ExpenseAttributeOrder other) {
    return category == other.category
        && identifiers.size() == other.identifiers.size()
        && new HashSet<>(identifiers).size() == identifiers.size()
        && new HashSet<>(identifiers).equals(new HashSet<>(other.identifiers));
  }

  public DocumentDigest digest() {
    return DocumentDigest.of(
        Map.of(
            "category",
            category.name(),
            "ids",
            identifiers.stream().map(ExpenseAttributeIdentifier::value).toList()));
  }
}
