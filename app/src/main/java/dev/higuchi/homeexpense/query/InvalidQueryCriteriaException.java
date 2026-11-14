package dev.higuchi.homeexpense.query;

/** クエリ条件が不正 */
public class InvalidQueryCriteriaException extends RuntimeException {
  public InvalidQueryCriteriaException(String message) {
    super(message);
  }
}
