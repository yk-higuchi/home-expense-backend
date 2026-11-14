package dev.yhiguchi.home_expense.presentation.api;

/** If-Match ヘッダーが RFC 9110 の構文に合わない（400） */
public class InvalidIfMatchHeaderException extends RuntimeException {
  public InvalidIfMatchHeaderException(String message) {
    super(message);
  }
}
