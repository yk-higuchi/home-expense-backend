package dev.higuchi.homeexpense.presentation.api;

import dev.higuchi.homeexpense.query.Granularity;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Function;

/**
 * クエリパラメータの型変換。変換できない値は 404 ではなく {@link InvalidQueryParameterException} (400) にする。 空文字は未指定 (null)
 * として扱う。
 *
 * <p>パスパラメータは対象外。変換できないパス値は「該当リソースなし」なので JAX-RS 標準どおり 404 とする。
 */
@Provider
public class QueryParamConverterProvider implements ParamConverterProvider {

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, Type genericType, Annotation[] annotations) {
    String name = queryParamName(annotations);
    if (name == null) {
      return null;
    }
    if (rawType == Integer.class) {
      return (ParamConverter<T>) new Converter<>(name, "整数", Integer::valueOf, String::valueOf);
    }
    if (rawType == LocalDate.class) {
      return (ParamConverter<T>)
          new Converter<>(name, "yyyy-MM-dd形式の日付", LocalDate::parse, LocalDate::toString);
    }
    if (rawType == Granularity.class) {
      return (ParamConverter<T>)
          new Converter<>(
              name,
              "day, week, month, quarter, yearのいずれか",
              value -> Granularity.valueOf(value.toUpperCase()),
              value -> value.name().toLowerCase());
    }
    return null;
  }

  private static String queryParamName(Annotation[] annotations) {
    return Arrays.stream(annotations)
        .filter(QueryParam.class::isInstance)
        .map(a -> ((QueryParam) a).value())
        .findFirst()
        .orElse(null);
  }

  private record Converter<T>(
      String name, String expected, Function<String, T> parser, Function<T, String> formatter)
      implements ParamConverter<T> {

    @Override
    public T fromString(String value) {
      if (value == null || value.isBlank()) {
        return null;
      }
      try {
        return parser.apply(value);
      } catch (RuntimeException e) {
        throw new InvalidQueryParameterException("%sは%sを指定してください".formatted(name, expected));
      }
    }

    @Override
    public String toString(T value) {
      return value == null ? null : formatter.apply(value);
    }
  }
}
