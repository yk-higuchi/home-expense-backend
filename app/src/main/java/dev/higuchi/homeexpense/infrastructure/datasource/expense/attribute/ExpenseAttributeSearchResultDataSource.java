package dev.higuchi.homeexpense.infrastructure.datasource.expense.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeDetail;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchResultQuerier;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExpenseAttributeSearchResultDataSource implements ExpenseAttributeSearchResultQuerier {

  private final JdbcOperator jdbc;

  public ExpenseAttributeSearchResultDataSource(JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<ExpenseAttributeDetail> list(Optional<ExpenseCategory> category) {
    String select = "SELECT id, category, name, document_digest FROM expense.attribute";
    String order = " ORDER BY CASE category WHEN '固定費' THEN 0 ELSE 1 END, sort_order, id";
    return category
        .map(value ->
            jdbc.queryForList(
                select + " WHERE category = ?" + order,
                ParameterBinder.of(value.name()),
                ExpenseAttributeSearchResultDataSource::mapDetail))
        .orElseGet(() ->
            jdbc.queryForList(
                select + order,
                ParameterBinder.EMPTY,
                ExpenseAttributeSearchResultDataSource::mapDetail));
  }

  @Override
  public Optional<ExpenseAttributeDetail> find(ExpenseAttributeIdentifier id) {
    return jdbc.queryForOptional(
        "SELECT id, category, name, document_digest FROM expense.attribute WHERE id = ?",
        ParameterBinder.of(id.value()),
        ExpenseAttributeSearchResultDataSource::mapDetail);
  }

  static ExpenseAttributeDetail mapDetail(ResultSet rs) throws SQLException {
    return new ExpenseAttributeDetail(
        rs.getString("id"),
        rs.getString("name"),
        rs.getString("category"),
        DocumentDigest.fromBytes(rs.getBytes("document_digest")));
  }
}
