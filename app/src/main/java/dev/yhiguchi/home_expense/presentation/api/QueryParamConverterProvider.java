package dev.yhiguchi.home_expense.presentation.api;

import dev.yhiguchi.home_expense.query.Granularity;
import jakarta.ws.rs.PathParam;
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
 * クエリ / パスパラメータの型変換。変換できない値は 404 ではなく {@link InvalidQueryParameterException} (400) にする。 空文字は未指定
 * (null) として扱う。
 */
@Provider
public class QueryParamConverterProvider implements ParamConverterProvider {

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, Type genericType, Annotation[] annotations) {
    String name = parameterName(annotations);
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

  private static String parameterName(Annotation[] annotations) {
    return Arrays.stream(annotations)
        .map(
            a ->
                switch (a) {
                  case QueryParam q -> q.value();
                  case PathParam p -> p.value();
                  default -> null;
                })
        .filter(n -> n != null)
        .findFirst()
        .orElse("パラメータ");
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
