package dev.higuchi.homeexpense.infrastructure.fake;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.*;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryExpenseRepository implements ExpenseRepository {

  private static final long INITIAL_VERSION = 1L;

  private record Stored(Expense entity, long version) {}

  private final Map<String, Stored> store = new LinkedHashMap<>();

  @Override
  public void register(Expense expense) {
    store.put(expense.expenseIdentifier().value(), new Stored(expense, INITIAL_VERSION));
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
  public void update(Expense expense, ExpectedVersion expectedVersion) {
    String key = expense.expenseIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedVersion.matches(current.version())) {
      throw new ConcurrentUpdateException();
    }
    store.put(key, new Stored(expense, current.version() + 1));
  }

  @Override
  public void verifyVersion(Expense expense, ExpectedVersion expectedVersion) {
    Stored current = store.get(expense.expenseIdentifier().value());
    if (current == null || !expectedVersion.matches(current.version())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void delete(Expense expense) {
    store.remove(expense.expenseIdentifier().value());
  }

  public List<Expense> all() {
    return store.values().stream().map(Stored::entity).toList();
  }

  public long versionOf(ExpenseIdentifier id) {
    Stored s = store.get(id.value());
    if (s == null) {
      throw new IllegalStateException("not found: " + id.value());
    }
    return s.version();
  }
}
