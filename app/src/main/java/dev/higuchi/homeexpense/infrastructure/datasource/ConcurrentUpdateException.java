package dev.higuchi.homeexpense.infrastructure.datasource;

public class ConcurrentUpdateException extends RuntimeException {
  public ConcurrentUpdateException() {
    super();
  }
}
