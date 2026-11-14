package dev.higuchi.homeexpense.infrastructure.fake;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryExpenseAttributeRepository implements ExpenseAttributeRepository {

  private static final long INITIAL_VERSION = 1L;

  private record Stored(ExpenseAttribute entity, long version) {}

  private final Map<String, Stored> store = new LinkedHashMap<>();

  @Override
  public void register(ExpenseAttribute expenseAttribute) {
    store.put(
        expenseAttribute.expenseAttributeIdentifier().value(),
        new Stored(expenseAttribute, INITIAL_VERSION));
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
  public void update(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion) {
    String key = expenseAttribute.expenseAttributeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedVersion.matches(current.version())) {
      throw new ConcurrentUpdateException();
    }
    store.put(key, new Stored(expenseAttribute, current.version() + 1));
  }

  @Override
  public void verifyVersion(ExpenseAttribute expenseAttribute, ExpectedVersion expectedVersion) {
    Stored current = store.get(expenseAttribute.expenseAttributeIdentifier().value());
    if (current == null || !expectedVersion.matches(current.version())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void delete(ExpenseAttribute expenseAttribute) {
    store.remove(expenseAttribute.expenseAttributeIdentifier().value());
  }

  public List<ExpenseAttribute> all() {
    return store.values().stream().map(Stored::entity).toList();
  }

  /** テスト用: 現在保持している version を取得 */
  public long versionOf(ExpenseAttributeIdentifier id) {
    Stored s = store.get(id.value());
    if (s == null) {
      throw new IllegalStateException("not found: " + id.value());
    }
    return s.version();
  }
}
