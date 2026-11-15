package dev.higuchi.homeexpense.infrastructure.fake;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryExpenseAttributeRepository implements ExpenseAttributeRepository {

  private record Stored(ExpenseAttribute entity, DocumentDigest digest) {}

  private final Map<String, Stored> store = new LinkedHashMap<>();

  @Override
  public void register(ExpenseAttribute expenseAttribute) {
    store.put(
        expenseAttribute.expenseAttributeIdentifier().value(),
        new Stored(expenseAttribute, expenseAttribute.digest()));
  }

  @Override
  public Optional<ExpenseAttribute> find(ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    return Optional.ofNullable(store.get(expenseAttributeIdentifier.value())).map(Stored::entity);
  }

  @Override
  public boolean existsByName(
      ExpenseAttributeName expenseAttributeName, ExpenseCategory expenseCategory) {
    return store.values().stream()
        .anyMatch(v ->
            v.entity().expenseAttributeName().equals(expenseAttributeName)
                && v.entity().expenseCategory() == expenseCategory);
  }

  @Override
  public void update(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest) {
    String key = expenseAttribute.expenseAttributeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.put(key, new Stored(expenseAttribute, expenseAttribute.digest()));
  }

  @Override
  public void verifyDigest(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest) {
    Stored current = store.get(expenseAttribute.expenseAttributeIdentifier().value());
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void delete(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest) {
    String key = expenseAttribute.expenseAttributeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.remove(key);
  }

  @Override
  public ExpenseAttributeOrder order(ExpenseCategory category) {
    return new ExpenseAttributeOrder(
        category,
        store.values().stream()
            .map(Stored::entity)
            .filter(e -> e.expenseCategory() == category)
            .map(ExpenseAttribute::expenseAttributeIdentifier)
            .toList());
  }

  @Override
  public void verifyOrder(ExpenseAttributeOrder current, ExpectedDigest expectedDigest) {
    if (!expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void reorder(ExpenseAttributeOrder order) {
    for (ExpenseAttributeIdentifier id : order.identifiers()) {
      Stored stored = store.remove(id.value());
      if (stored != null) {
        store.put(id.value(), stored);
      }
    }
  }

  public List<ExpenseAttribute> all() {
    return store.values().stream().map(Stored::entity).toList();
  }

  public DocumentDigest digestOf(ExpenseAttributeIdentifier id) {
    Stored s = store.get(id.value());
    if (s == null) {
      throw new IllegalStateException("not found: " + id.value());
    }
    return s.digest();
  }
}
