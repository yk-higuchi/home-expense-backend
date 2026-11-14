package dev.higuchi.homeexpense.infrastructure.datasource;

/** 並行更新例外 */
public class ConcurrentUpdateException extends RuntimeException {
  public ConcurrentUpdateException() {
    super();
  }
}
