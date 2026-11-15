package dev.higuchi.homeexpense.infrastructure.datasource.income.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.datasource.UniqueConstraintViolationException;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IncomeAttributeDataSource implements IncomeAttributeRepository {

  private final JdbcOperator jdbc;

  public IncomeAttributeDataSource(JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void register(IncomeAttribute incomeAttribute) {
    try {
      jdbc.update(
          """
          INSERT INTO expense.income_attribute(id, name, document_digest, sort_order)
          SELECT ?, ?, ?, COALESCE(MAX(sort_order), 0) + 1
          FROM expense.income_attribute
          """,
          ParameterBinder.of(
              incomeAttribute.incomeAttributeIdentifier().value(),
              incomeAttribute.incomeAttributeName().value(),
              incomeAttribute.digest().toBytes()));
    } catch (UniqueConstraintViolationException e) {
      throw new IncomeAttributeAlreadyExistsException();
    }
  }

  @Override
  public void update(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        "UPDATE expense.income_attribute SET name = ?, document_digest = ? WHERE id = ?",
        List.of(
            incomeAttribute.incomeAttributeName().value(),
            incomeAttribute.digest().toBytes(),
            incomeAttribute.incomeAttributeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void verifyDigest(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest) {
    jdbc.verifyDigest(
        "SELECT 1 FROM expense.income_attribute WHERE id = ?",
        List.of(incomeAttribute.incomeAttributeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void delete(IncomeAttribute incomeAttribute, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        "DELETE FROM expense.income_attribute WHERE id = ?",
        List.of(incomeAttribute.incomeAttributeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public Optional<IncomeAttribute> find(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    return jdbc.queryForOptional(
        "SELECT id, name FROM expense.income_attribute WHERE id = ?",
        ParameterBinder.of(incomeAttributeIdentifier.value()),
        IncomeAttributeDataSource::mapIncomeAttribute);
  }

  @Override
  public boolean existsByName(IncomeAttributeName incomeAttributeName) {
    String sql =
        """
        SELECT CASE WHEN EXISTS(SELECT 1 FROM expense.income_attribute WHERE name = ?)
          THEN TRUE ELSE FALSE END
        FROM (VALUES (1)) AS t(x)
        """;
    return jdbc.queryForOptional(
            sql, ParameterBinder.of(incomeAttributeName.value()), rs -> rs.getBoolean(1))
        .orElse(false);
  }

  @Override
  public IncomeAttributeList list() {
    return new IncomeAttributeList(
        jdbc.queryForList(
            "SELECT id, name FROM expense.income_attribute ORDER BY sort_order, id FOR UPDATE",
            ParameterBinder.EMPTY,
            IncomeAttributeDataSource::mapIncomeAttribute));
  }

  @Override
  public void verifyList(IncomeAttributeList current, ExpectedDigest expectedDigest) {
    if (!expectedDigest.matches(current.digest())) {
      throw new ConcurrentUpdateException();
    }
  }

  @Override
  public void reorder(IncomeAttributeList list) {
    List<IncomeAttributeIdentifier> identifiers = list.identifiers();
    for (int i = 0; i < identifiers.size(); i++) {
      jdbc.update(
          "UPDATE expense.income_attribute SET sort_order = ? WHERE id = ?",
          ParameterBinder.of(i + 1, identifiers.get(i).value()));
    }
  }

  static IncomeAttribute mapIncomeAttribute(ResultSet rs) throws SQLException {
    return new IncomeAttribute(
        new IncomeAttributeIdentifier(rs.getString("id")),
        new IncomeAttributeName(rs.getString("name")));
  }
}
