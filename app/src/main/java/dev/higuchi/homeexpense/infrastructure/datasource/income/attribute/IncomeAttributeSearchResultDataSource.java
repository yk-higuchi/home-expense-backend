package dev.higuchi.homeexpense.infrastructure.datasource.income.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ReadOnly;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeDetail;
import dev.higuchi.homeexpense.query.income.attribute.IncomeAttributeSearchResultQuerier;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class IncomeAttributeSearchResultDataSource implements IncomeAttributeSearchResultQuerier {

  private final JdbcOperator jdbc;

  public IncomeAttributeSearchResultDataSource(@ReadOnly JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<IncomeAttributeDetail> list() {
    return jdbc.queryForList(
        "SELECT id, name, document_digest FROM expense.income_attribute ORDER BY sort_order, id",
        ParameterBinder.EMPTY,
        IncomeAttributeSearchResultDataSource::mapDetail);
  }

  @Override
  public Optional<IncomeAttributeDetail> find(IncomeAttributeIdentifier id) {
    return jdbc.queryForOptional(
        "SELECT id, name, document_digest FROM expense.income_attribute WHERE id = ?",
        ParameterBinder.of(id.value()),
        IncomeAttributeSearchResultDataSource::mapDetail);
  }

  static IncomeAttributeDetail mapDetail(ResultSet rs) throws SQLException {
    return new IncomeAttributeDetail(
        rs.getString("id"),
        rs.getString("name"),
        DocumentDigest.fromBytes(rs.getBytes("document_digest")));
  }
}
