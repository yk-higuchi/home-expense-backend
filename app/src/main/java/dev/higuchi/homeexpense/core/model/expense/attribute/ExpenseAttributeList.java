package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.AttributeMove;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ExpenseAttributeList(ExpenseCategory category, List<ExpenseAttribute> attributes) {

  public static final int MAX_SIZE = 100;

  public ExpenseAttributeList {
    Objects.requireNonNull(category, "分類は必須です");
    attributes = List.copyOf(attributes);
  }

  public int size() {
    return attributes.size();
  }

  public ExpenseAttributeList moved(List<AttributeMove> moves) {
    return new ExpenseAttributeList(category, AttributeMove.applyAll(attributes, moves));
  }

  public boolean hasSameOrderAs(ExpenseAttributeList other) {
    return identifiers().equals(other.identifiers());
  }

  public List<ExpenseAttributeIdentifier> identifiers() {
    return attributes.stream().map(ExpenseAttribute::expenseAttributeIdentifier).toList();
  }

  public DocumentDigest digest() {
    return digestOf(attributes);
  }

  public static DocumentDigest digestOf(List<ExpenseAttribute> attributes) {
    return DocumentDigest.of(
        Map.of(
            "expense_attributes",
            attributes.stream()
                .map(attribute ->
                    Map.of(
                        "category", attribute.expenseCategory().name(),
                        "id", attribute.expenseAttributeIdentifier().value(),
                        "name", attribute.expenseAttributeName().value()))
                .toList()));
  }
}
