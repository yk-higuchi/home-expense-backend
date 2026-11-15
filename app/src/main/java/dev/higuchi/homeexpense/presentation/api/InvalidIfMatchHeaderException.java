package dev.higuchi.homeexpense.presentation.api;

public class InvalidIfMatchHeaderException extends RuntimeException {
  public InvalidIfMatchHeaderException(String message) {
    super(message);
  }
}
