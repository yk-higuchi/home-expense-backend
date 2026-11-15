package dev.higuchi.homeexpense.infrastructure.datasource.expense;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.*;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExpenseDataSource implements ExpenseRepository {

  private final JdbcOperator jdbc;

  public ExpenseDataSource(JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void register(Expense expense) {
    jdbc.update(
        "INSERT INTO expense.expense(id, description, amount, payment_date, attribute_id, document_digest)"
            + " VALUES (?, ?, ?, ?, ?, ?)",
        ParameterBinder.of(
            expense.expenseIdentifier().value(),
            expense.description().value(),
            expense.amount().value(),
            expense.paymentDate().value(),
            expense.expenseAttributeIdentifier().value(),
            expense.digest().toBytes()));
  }

  @Override
  public Optional<Expense> find(ExpenseIdentifier expenseIdentifier) {
    String sql =
        """
        SELECT id, description, amount, payment_date, attribute_id
        FROM expense.expense
        WHERE id = ?
        """;
    return jdbc.queryForOptional(
        sql, ParameterBinder.of(expenseIdentifier.value()), ExpenseDataSource::mapExpense);
  }

  @Override
  public boolean existsByAttributeIdentifier(
      ExpenseAttributeIdentifier expenseAttributeIdentifier) {
    String sql =
        """
        SELECT CASE WHEN EXISTS(
          SELECT 1 FROM expense.expense WHERE attribute_id = ?
        ) THEN TRUE ELSE FALSE END
        FROM (VALUES (1)) AS t(x)
        """;
    return jdbc.queryForOptional(
            sql, ParameterBinder.of(expenseAttributeIdentifier.value()), rs -> rs.getBoolean(1))
        .orElse(false);
  }

  @Override
  public void update(Expense expense, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        """
        UPDATE expense.expense
        SET description = ?, amount = ?, payment_date = ?, attribute_id = ?, document_digest = ?
        WHERE id = ?
        """,
        List.of(
            expense.description().value(),
            expense.amount().value(),
            expense.paymentDate().value(),
            expense.expenseAttributeIdentifier().value(),
            expense.digest().toBytes(),
            expense.expenseIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void verifyDigest(Expense expense, ExpectedDigest expectedDigest) {
    jdbc.verifyDigest(
        "SELECT 1 FROM expense.expense WHERE id = ?",
        List.of(expense.expenseIdentifier().value()),
        expectedDigest);
  }

  @Override
  public void delete(Expense expense, ExpectedDigest expectedDigest) {
    jdbc.updateWithOptimisticLock(
        "DELETE FROM expense.expense WHERE expense.id = ?",
        List.of(expense.expenseIdentifier().value()),
        expectedDigest);
  }

  static Expense mapExpense(ResultSet rs) throws SQLException {
    return new Expense(
        new ExpenseIdentifier(rs.getString("id")),
        new Description(rs.getString("description")),
        new Amount(rs.getInt("amount")),
        new PaymentDate(rs.getObject("payment_date", LocalDate.class)),
        new ExpenseAttributeIdentifier(rs.getString("attribute_id")));
  }
}
