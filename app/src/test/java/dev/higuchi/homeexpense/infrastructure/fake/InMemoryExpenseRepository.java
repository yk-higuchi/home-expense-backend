package dev.higuchi.homeexpense.infrastructure.fake;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.*;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryExpenseRepository implements ExpenseRepository {

  private record Stored(Expense entity, DocumentDigest digest) {}

  private final Map<String, Stored> store = new LinkedHashMap<>();

  @Override
  public void register(Expense expense) {
    store.put(expense.expenseIdentifier().value(), new Stored(expense, expense.digest()));
  }

  @Override
  public Optional<Expense> find(ExpenseIdentifier expenseIdentifier) {
    return Optional.ofNullable(store.get(expenseIdentifier.value())).map(Stored::entity);
  }

  @Override
  public boolean existsByAttributeIdentifier(
      ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    return store.values().stream()
        .anyMatch(v -> v.entity().expenseAttributeIdentifier().equals(expenseAttributeIdentifier));
  }

  @Override
  public void update(Expense expense, ExpectedDigest expectedDigest) {
    String key = expense.expenseIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.put(key, new Stored(expense, expense.digest()));
  }

  @Override
  public void verifyDigest(Expense expense, ExpectedDigest expectedDigest) {
    Stored current = store.get(expense.expenseIdentifier().value());
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void delete(Expense expense, ExpectedDigest expectedDigest) {
    String key = expense.expenseIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.remove(key);
  }

  public List<Expense> all() {
    return store.values().stream().map(Stored::entity).toList();
  }

  public DocumentDigest digestOf(ExpenseIdentifier id) {
    Stored s = store.get(id.value());
    if (s == null) {
      throw new IllegalStateException("not found: " + id.value());
    }
    return s.digest();
  }
}
