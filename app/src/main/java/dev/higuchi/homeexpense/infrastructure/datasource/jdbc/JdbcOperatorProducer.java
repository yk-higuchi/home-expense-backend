package dev.higuchi.homeexpense.infrastructure.datasource.jdbc;

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
}
