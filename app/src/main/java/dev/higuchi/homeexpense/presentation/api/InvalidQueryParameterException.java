package dev.higuchi.homeexpense.presentation.api;

import jakarta.ws.rs.BadRequestException;

public class InvalidQueryParameterException extends BadRequestException {
  public InvalidQueryParameterException(String message) {
    super(message);
  }
}
