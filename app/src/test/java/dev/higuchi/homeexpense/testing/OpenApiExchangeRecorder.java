package dev.higuchi.homeexpense.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

public class OpenApiExchangeRecorder implements QuarkusTestBeforeEachCallback, Filter {

  static final String FILE_PROPERTY = "openapi.exchanges.file";

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final OpenApiExchangeRecorder INSTANCE = new OpenApiExchangeRecorder();
  private static volatile String currentTest = "";

  @Override
  public void beforeEach(QuarkusTestMethodContext context) {
    if (System.getProperty(FILE_PROPERTY) == null) {
      return;
    }
    currentTest =
        context.getTestInstance().getClass().getName() + "#" + context.getTestMethod().getName();
    if (RestAssured.filters().stream().noneMatch(OpenApiExchangeRecorder.class::isInstance)) {
      RestAssured.filters(INSTANCE);
    }
  }

  @Override
  public Response filter(
      FilterableRequestSpecification request,
      FilterableResponseSpecification response,
      FilterContext context) {
    Response result = context.next(request, response);
    String file = System.getProperty(FILE_PROPERTY);
    if (file == null) {
      return result;
    }
    Map<String, Object> exchange = new LinkedHashMap<>();
    exchange.put("test", currentTest);
    exchange.put("method", request.getMethod());
    exchange.put("uri", request.getURI());
    exchange.put("request_headers", headers(request.getHeaders()));
    exchange.put("request_content_type", request.getContentType());
    exchange.put("request_body", body(request.getBody()));
    exchange.put("status", result.getStatusCode());
    exchange.put("response_headers", headers(result.getHeaders()));
    exchange.put("response_content_type", result.getContentType());
    exchange.put("response_body", result.asString());
    append(Path.of(file), exchange);
    return result;
  }

  private static Map<String, String> headers(Iterable<Header> headers) {
    Map<String, String> map = new LinkedHashMap<>();
    for (Header header : headers) {
      map.merge(header.getName(), header.getValue(), (a, b) -> a + ", " + b);
    }
    return map;
  }

  private static String body(Object body) {
    return switch (body) {
      case null -> null;
      case String s -> s;
      case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
      default -> {
        try {
          yield MAPPER.writeValueAsString(body);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    };
  }

  private static synchronized void append(Path file, Map<String, Object> exchange) {
    try {
      Files.createDirectories(file.toAbsolutePath().getParent());
      Files.writeString(
          file,
          MAPPER.writeValueAsString(exchange) + "\n",
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
