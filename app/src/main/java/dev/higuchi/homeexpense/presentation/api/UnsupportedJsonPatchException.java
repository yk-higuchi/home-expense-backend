package dev.higuchi.homeexpense.presentation.api;

public class UnsupportedJsonPatchException extends RuntimeException {
  public UnsupportedJsonPatchException(String message) {
    super(message);
  }
}
