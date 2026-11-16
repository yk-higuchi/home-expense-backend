package dev.higuchi.homeexpense.infrastructure.fake;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.income.*;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import java.util.*;
import java.util.Optional;

public class InMemoryIncomeRepository implements IncomeRepository {

  private record Stored(Income entity, DocumentDigest digest) {}

  private final Map<String, Stored> store = new LinkedHashMap<>();

  @Override
  public void register(Income income) {
    store.put(income.incomeIdentifier().value(), new Stored(income, income.digest()));
  }

  @Override
  public void update(Income income, ExpectedDigest expectedDigest) {
    String key = income.incomeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.put(key, new Stored(income, income.digest()));
  }

  @Override
  public void verifyDigest(Income income, ExpectedDigest expectedDigest) {
    Stored current = store.get(income.incomeIdentifier().value());
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void delete(Income income, ExpectedDigest expectedDigest) {
    String key = income.incomeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.remove(key);
  }

  @Override
  public Optional<Income> find(IncomeIdentifier incomeIdentifier) {
    return Optional.ofNullable(store.get(incomeIdentifier.value())).map(Stored::entity);
  }

  @Override
  public boolean existsByAttributeIdentifier(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    return store.values().stream()
        .anyMatch(v -> v.entity().incomeAttributeIdentifier().equals(incomeAttributeIdentifier));
  }

  public List<Income> all() {
    return store.values().stream().map(Stored::entity).toList();
  }

  public DocumentDigest digestOf(IncomeIdentifier id) {
    Stored s = store.get(id.value());
    if (s == null) {
      throw new IllegalStateException("not found: " + id.value());
    }
    return s.digest();
  }
}
