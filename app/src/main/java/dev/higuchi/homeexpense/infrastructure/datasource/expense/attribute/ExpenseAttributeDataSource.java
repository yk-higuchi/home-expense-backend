package dev.higuchi.homeexpense.infrastructure.datasource.expense.attribute;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.UniqueConstraintViolationException;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExpenseAttributeDataSource implements ExpenseAttributeRepository {

  private final JdbcOperator jdbc;

  public ExpenseAttributeDataSource(JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void register(ExpenseAttribute expenseAttribute) {
    try {
      jdbc.update(
          "INSERT INTO expense.attribute(id, category, name, document_digest) VALUES (?, ?, ?, ?)",
          ParameterBinder.of(
              expenseAttribute.expenseAttributeIdentifier().value(),
              expenseAttribute.expenseCategory().name(),
              expenseAttribute.expenseAttributeName().value(),
              expenseAttribute.digest().toBytes()));
    } catch (UniqueConstraintViolationException e) {
      throw new ExpenseAttributeAlreadyExistsException();
    }
  }

  @Override
  public Optional<ExpenseAttribute> find(ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    return jdbc.queryForOptional(
        "SELECT id, category, name FROM expense.attribute WHERE id = ?",
        ParameterBinder.of(expenseAttributeIdentifier.value()),
        ExpenseAttributeDataSource::mapExpenseAttribute);
  }

  @Override
  public boolean existsByName(
      ExpenseAttributeName expenseAttributeName, ExpenseCategory expenseCategory) {
    String sql =
        """
        SELECT CASE WHEN EXISTS(
            SELECT 1 FROM expense.attribute WHERE name = ? AND category = ?)
          THEN TRUE ELSE FALSE END
        FROM (VALUES (1)) AS t(x)
        """;
    return jdbc.queryForOptional(
            sql,
            ParameterBinder.of(expenseAttributeName.value(), expenseCategory.name()),
            rs -> rs.getBoolean(1))
        .orElse(false);
  }

  @Override
  public void update(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        "UPDATE expense.attribute SET category = ?, name = ?, document_digest = ? WHERE id = ?",
        List.of(
            expenseAttribute.expenseCategory().name(),
            expenseAttribute.expenseAttributeName().value(),
            expenseAttribute.digest().toBytes(),
            expenseAttribute.expenseAttributeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void verifyDigest(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest) {
    jdbc.verifyDigest(
        "SELECT 1 FROM expense.attribute WHERE id = ?",
        List.of(expenseAttribute.expenseAttributeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void delete(ExpenseAttribute expenseAttribute, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        "DELETE FROM expense.attribute WHERE id = ?",
        List.of(expenseAttribute.expenseAttributeIdentifier().value()),
        expectedDigest);
  }

  static ExpenseAttribute mapExpenseAttribute(ResultSet rs) throws SQLException {
    return new ExpenseAttribute(
        new ExpenseAttributeIdentifier(rs.getString("id")),
        new ExpenseAttributeName(rs.getString("name")),
        ExpenseCategory.valueOf(rs.getString("category")));
  }
}
