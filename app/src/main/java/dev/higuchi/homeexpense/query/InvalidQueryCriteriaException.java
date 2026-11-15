package dev.higuchi.homeexpense.query;

public class InvalidQueryCriteriaException extends RuntimeException {
  public InvalidQueryCriteriaException(String message) {
    super(message);
  }
}
