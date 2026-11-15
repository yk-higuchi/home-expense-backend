package dev.higuchi.homeexpense.infrastructure.datasource.income;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.*;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IncomeDataSource implements IncomeRepository {

  private final JdbcOperator jdbc;

  public IncomeDataSource(JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void register(Income income) {
    jdbc.update(
        "INSERT INTO expense.income(id, attribute_id, description, amount, receive_date, document_digest) VALUES (?, ?, ?, ?, ?, ?)",
        ParameterBinder.of(
            income.incomeIdentifier().value(),
            income.incomeAttributeIdentifier().value(),
            income.description().value(),
            income.amount().value(),
            income.receiveDate().value(),
            income.digest().toBytes()));
  }

  @Override
  public void update(Income income, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        """
        UPDATE expense.income
        SET attribute_id = ?, description = ?, amount = ?, receive_date = ?, document_digest = ?
        WHERE id = ?
        """,
        List.of(
            income.incomeAttributeIdentifier().value(),
            income.description().value(),
            income.amount().value(),
            income.receiveDate().value(),
            income.digest().toBytes(),
            income.incomeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void verifyDigest(Income income, ExpectedDigest expectedDigest) {
    jdbc.verifyDigest(
        "SELECT 1 FROM expense.income WHERE id = ?",
        List.of(income.incomeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void delete(Income income, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        "DELETE FROM expense.income WHERE income.id = ?",
        List.of(income.incomeIdentifier().value()),
        expectedDigest);
  }

  @Override
  public Optional<Income> find(IncomeIdentifier incomeIdentifier) {
    return jdbc.queryForOptional(
        """
        SELECT id, attribute_id, description, amount, receive_date
        FROM expense.income
        WHERE id = ?
        """,
        ParameterBinder.of(incomeIdentifier.value()),
        IncomeDataSource::mapIncome);
  }

  @Override
  public boolean existsByAttributeIdentifier(IncomeAttributeIdentifier incomeAttributeIdentifier) {
    String sql =
        """
        SELECT CASE WHEN EXISTS(SELECT 1 FROM expense.income WHERE attribute_id = ?)
          THEN TRUE ELSE FALSE END
        FROM (VALUES (1)) AS t(x)
        """;
    return jdbc.queryForOptional(
            sql, ParameterBinder.of(incomeAttributeIdentifier.value()), rs -> rs.getBoolean(1))
        .orElse(false);
  }

  static Income mapIncome(ResultSet rs) throws SQLException {
    return new Income(
        new IncomeIdentifier(rs.getString("id")),
        new Description(rs.getString("description")),
        new Amount(rs.getInt("amount")),
        new ReceiveDate(rs.getObject("receive_date", LocalDate.class)),
        new IncomeAttributeIdentifier(rs.getString("attribute_id")));
  }
}
