package dev.higuchi.homeexpense.infrastructure.datasource.jdbc;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.infrastructure.datasource.DataAccessException;
import dev.higuchi.homeexpense.infrastructure.datasource.UniqueConstraintViolationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcOperator {

  private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

  private final DataSource dataSource;

  public JdbcOperator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public <T> Optional<T> queryForOptional(String sql, ParameterBinder binder, RowMapper<T> mapper) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      binder.bind(ps);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.ofNullable(mapper.map(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw translate(e);
    }
  }

  public <T> List<T> queryForList(String sql, ParameterBinder binder, RowMapper<T> mapper) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      binder.bind(ps);
      try (ResultSet rs = ps.executeQuery()) {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
          list.add(mapper.map(rs));
        }
        return list;
      }
    } catch (SQLException e) {
      throw translate(e);
    }
  }

  public int update(String sql, ParameterBinder binder) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      binder.bind(ps);
      return ps.executeUpdate();
    } catch (SQLException e) {
      throw translate(e);
    }
  }

  public void updateWithOptimisticLock(String sql, List<Object> params, ExpectedDigest expected) {
    DigestCondition condition = DigestCondition.of(params, expected);
    if (update(sql.stripTrailing() + condition.sql(), condition.binder()) == 0) {
      throw new ConcurrentUpdateException();
    }
  }

  public void verifyDigest(String sql, List<Object> params, ExpectedDigest expected) {
    DigestCondition condition = DigestCondition.of(params, expected);
    if (queryForOptional(sql.stripTrailing() + condition.sql(), condition.binder(), rs -> true)
        .isEmpty()) {
      throw new ConcurrentUpdateException();
    }
  }

  private record DigestCondition(String sql, List<Object> params) {

    static DigestCondition of(List<Object> baseParams, ExpectedDigest expected) {
      List<Object> params = new ArrayList<>(baseParams);
      String sql =
          switch (expected) {
            case ExpectedDigest.Unconditional u -> "";
            case ExpectedDigest.OneOf oneOf -> {
              if (oneOf.digests().isEmpty()) {
                throw new ConcurrentUpdateException();
              }
              oneOf.digests().forEach(digest -> params.add(digest.toBytes()));
              yield " AND document_digest IN ("
                  + String.join(", ", Collections.nCopies(oneOf.digests().size(), "?"))
                  + ")";
            }
          };
      return new DigestCondition(sql, params);
    }

    ParameterBinder binder() {
      return ParameterBinder.of(params.toArray());
    }
  }

  private static DataAccessException translate(SQLException cause) {
    if (SQLSTATE_UNIQUE_VIOLATION.equals(cause.getSQLState())) {
      return new UniqueConstraintViolationException(cause);
    }
    return new DataAccessException(cause);
  }
}
