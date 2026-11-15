package dev.higuchi.homeexpense.infrastructure.datasource;

import java.sql.SQLException;

public class UniqueConstraintViolationException extends DataAccessException {

  public UniqueConstraintViolationException(SQLException cause) {
    super(cause);
  }
}
