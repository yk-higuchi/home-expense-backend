package dev.yhiguchi.home_expense.presentation.api;

import dev.yhiguchi.home_expense.query.Granularity;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class GranularityParamConverterProvider implements ParamConverterProvider {

  private static final ParamConverter<Granularity> CONVERTER =
      new ParamConverter<>() {
        @Override
        public Granularity fromString(String value) {
          if (value == null || value.isBlank()) {
            return null;
          }
          try {
            return Granularity.valueOf(value.toUpperCase());
          } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                "granularityはday, week, month, quarter, yearのいずれかを指定してください");
          }
        }

        @Override
        public String toString(Granularity value) {
          return value.name().toLowerCase();
        }
      };

  @Override
  @SuppressWarnings("unchecked")
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, Type genericType, Annotation[] annotations) {
    return rawType == Granularity.class ? (ParamConverter<T>) CONVERTER : null;
  }
}
