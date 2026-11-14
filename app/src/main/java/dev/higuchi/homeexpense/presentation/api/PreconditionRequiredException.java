package dev.higuchi.homeexpense.presentation.api;

/** 条件付きリクエストが必須の操作で If-Match が指定されていない（428, RFC 6585 §3） */
public class PreconditionRequiredException extends RuntimeException {
  public PreconditionRequiredException(String message) {
    super(message);
  }
}
