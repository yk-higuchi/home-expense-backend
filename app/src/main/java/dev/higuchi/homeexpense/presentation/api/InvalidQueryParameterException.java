package dev.higuchi.homeexpense.presentation.api;

import jakarta.ws.rs.BadRequestException;

/**
 * クエリパラメータの値を変換できない。
 *
 * <p>RESTEasy Reactive はパラメータ変換中の例外を 404 に包み直すが、{@link jakarta.ws.rs.WebApplicationException}
 * はそのまま伝播するため {@link BadRequestException} を継承している。
 */
public class InvalidQueryParameterException extends BadRequestException {
  public InvalidQueryParameterException(String message) {
    super(message);
  }
}
