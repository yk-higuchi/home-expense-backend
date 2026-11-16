package dev.higuchi.homeexpense.presentation.api;

public class PreconditionRequiredException extends RuntimeException {
  public PreconditionRequiredException(String message) {
    super(message);
  }
}
