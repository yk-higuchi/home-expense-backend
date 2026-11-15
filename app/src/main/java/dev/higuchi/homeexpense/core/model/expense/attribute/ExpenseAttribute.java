package dev.higuchi.homeexpense.core.model.expense.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ExpenseAttribute {
  ExpenseAttributeIdentifier expenseAttributeIdentifier;
  ExpenseAttributeName expenseAttributeName;
  ExpenseCategory expenseCategory;

  public ExpenseAttribute(
      ExpenseAttributeIdentifier expenseAttributeIdentifier,
      ExpenseAttributeName expenseAttributeName,
      ExpenseCategory expenseCategory) {
    this.expenseAttributeIdentifier =
        Objects.requireNonNull(expenseAttributeIdentifier, "経費属性識別子は必須です");
    this.expenseAttributeName = Objects.requireNonNull(expenseAttributeName, "経費属性名は必須です");
    this.expenseCategory = Objects.requireNonNull(expenseCategory, "経費分類は必須です");
  }

  public static ExpenseAttribute create(
      ExpenseAttributeName expenseAttributeName, ExpenseCategory expenseCategory) {
    ExpenseAttributeIdentifier expenseAttributeIdentifier =
        new ExpenseAttributeIdentifier(UUID.randomUUID().toString());
    return new ExpenseAttribute(expenseAttributeIdentifier, expenseAttributeName, expenseCategory);
  }

  public ExpenseAttribute updateWith(ExpenseAttributeName expenseAttributeName) {
    return new ExpenseAttribute(
        this.expenseAttributeIdentifier, expenseAttributeName, this.expenseCategory);
  }

  public boolean hasChanges(ExpenseAttribute other) {
    return !Objects.equals(expenseAttributeName, other.expenseAttributeName);
  }

  public boolean hasSameName(ExpenseAttributeName expenseAttributeName) {
    return Objects.equals(this.expenseAttributeName, expenseAttributeName);
  }

  public boolean isFixed() {
    return expenseCategory.isFixed();
  }

  public boolean isVariable() {
    return expenseCategory.isVariable();
  }

  public ExpenseAttributeIdentifier expenseAttributeIdentifier() {
    return expenseAttributeIdentifier;
  }

  public ExpenseAttributeName expenseAttributeName() {
    return expenseAttributeName;
  }

  public ExpenseCategory expenseCategory() {
    return expenseCategory;
  }

  public DocumentDigest digest() {
    return DocumentDigest.of(
        Map.of("category", expenseCategory.name(), "name", expenseAttributeName.value()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExpenseAttribute that = (ExpenseAttribute) o;
    return Objects.equals(expenseAttributeIdentifier, that.expenseAttributeIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(expenseAttributeIdentifier);
  }
}
