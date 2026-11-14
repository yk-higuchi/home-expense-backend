package dev.yhiguchi.home_expense.infrastructure.datasource.jdbc;

import dev.yhiguchi.home_expense.infrastructure.datasource.ReadOnly;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import javax.sql.DataSource;

@ApplicationScoped
public class JdbcOperatorProducer {

  @Produces
  @ApplicationScoped
  public JdbcOperator primary(DataSource dataSource) {
    return new JdbcOperator(dataSource);
  }

  @Produces
  @ApplicationScoped
  @ReadOnly
  public JdbcOperator readonly(@io.quarkus.agroal.DataSource("readonly") DataSource dataSource) {
    return new JdbcOperator(dataSource);
  }
}
