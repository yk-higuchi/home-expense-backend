package dev.higuchi.homeexpense.infrastructure.datasource.expense.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.infrastructure.datasource.ReadOnly;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.JdbcOperator;
import dev.higuchi.homeexpense.infrastructure.datasource.jdbc.ParameterBinder;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeDetail;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchCriteria;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchResult;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeSearchResultQuerier;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ExpenseAttributeSearchResultDataSource implements ExpenseAttributeSearchResultQuerier {

  private final JdbcOperator jdbc;

  public ExpenseAttributeSearchResultDataSource(@ReadOnly JdbcOperator jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public ExpenseAttributeSearchResult search(ExpenseAttributeSearchCriteria criteria) {
    int count = selectCount(criteria);
    if (count == 0) {
      return ExpenseAttributeSearchResult.empty();
    }
    List<ExpenseAttributeDetail> list = selectBy(criteria);
    return new ExpenseAttributeSearchResult(count, list);
  }

  @Override
  public Optional<ExpenseAttributeDetail> find(ExpenseAttributeIdentifier id) {
    return jdbc.queryForOptional(
        "SELECT id, category, name, document_digest FROM expense.attribute WHERE id = ?",
        ParameterBinder.of(id.value()),
        ExpenseAttributeSearchResultDataSource::mapDetail);
  }

  private int selectCount(ExpenseAttributeSearchCriteria criteria) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM expense.attribute");
    List<Object> params = new ArrayList<>();
    appendCategoryFilter(criteria, sql, params);
    return jdbc.queryForOptional(
            sql.toString(), ParameterBinder.of(params.toArray()), rs -> rs.getInt(1))
        .orElse(0);
  }

  private List<ExpenseAttributeDetail> selectBy(ExpenseAttributeSearchCriteria criteria) {
    StringBuilder sql =
        new StringBuilder("SELECT id, category, name, document_digest FROM expense.attribute");
    List<Object> params = new ArrayList<>();
    appendCategoryFilter(criteria, sql, params);
    sql.append(" ORDER BY attribute.created_at OFFSET ? ROWS FETCH FIRST ? ROWS ONLY");
    params.add(criteria.offset());
    params.add(criteria.perPage());
    return jdbc.queryForList(
        sql.toString(),
        ParameterBinder.of(params.toArray()),
        ExpenseAttributeSearchResultDataSource::mapDetail);
  }

  private void appendCategoryFilter(
      ExpenseAttributeSearchCriteria criteria, StringBuilder sql, List<Object> params) {
    if (criteria.expenseCategory() != null) {
      sql.append(" WHERE attribute.category = ?");
      params.add(criteria.expenseCategory());
    }
  }

  static ExpenseAttributeDetail mapDetail(ResultSet rs) throws SQLException {
    return new ExpenseAttributeDetail(
        rs.getString("id"),
        rs.getString("name"),
        rs.getString("category"),
        DocumentDigest.fromBytes(rs.getBytes("document_digest")));
  }
}
