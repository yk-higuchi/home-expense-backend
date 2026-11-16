package dev.higuchi.homeexpense.infrastructure.fake;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryIncomeAttributeRepository implements IncomeAttributeRepository {

  private record Stored(IncomeAttribute entity, DocumentDigest digest) {}

  private final Map<String, Stored> store = new LinkedHashMap<>();

  @Override
  public void register(IncomeAttribute incomeAttribute) {
    store.put(
        incomeAttribute.incomeAttributeIdentifier().value(),
        new Stored(incomeAttribute, incomeAttribute.digest()));
  }

  @Override
  public void update(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest) {
    String key = incomeAttribute.incomeAttributeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.put(key, new Stored(incomeAttribute, incomeAttribute.digest()));
  }

  @Override
  public void verifyDigest(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest) {
    Stored current = store.get(incomeAttribute.incomeAttributeIdentifier().value());
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void delete(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest) {
    String key = incomeAttribute.incomeAttributeIdentifier().value();
    Stored current = store.get(key);
    if (current == null || !expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
    store.remove(key);
  }

  @Override
  public Optional<IncomeAttribute> find(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    return Optional.ofNullable(store.get(incomeAttributeIdentifier.value())).map(Stored::entity);
  }

  @Override
  public boolean existsByName(IncomeAttributeName incomeAttributeName) {
    return store.values().stream()
        .anyMatch(v -> v.entity().incomeAttributeName().equals(incomeAttributeName));
  }

  @Override
  public IncomeAttributeList list() {
    return new IncomeAttributeList(store.values().stream().map(Stored::entity).toList());
  }

  @Override
  public void verifyList(IncomeAttributeList current, ExpectedDigest expectedDigest) {
    if (!expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void reorder(IncomeAttributeList list) {
    for (IncomeAttributeIdentifier id : list.identifiers()) {
      Stored stored = store.remove(id.value());
      if (stored != null) {
        store.put(id.value(), stored);
      }
    }
  }

  public List<IncomeAttribute> all() {
    return store.values().stream().map(Stored::entity).toList();
  }

  public DocumentDigest digestOf(IncomeAttributeIdentifier id) {
    Stored s = store.get(id.value());
    if (s == null) {
      throw new IllegalStateException("not found: " + id.value());
    }
    return s.digest();
  }
}
