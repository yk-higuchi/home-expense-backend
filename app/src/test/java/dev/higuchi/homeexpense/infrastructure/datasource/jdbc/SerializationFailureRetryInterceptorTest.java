package dev.higuchi.homeexpense.infrastructure.datasource.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.infrastructure.datasource.DataAccessException;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SerializationFailureRetryInterceptorTest {

  private final SerializationFailureRetryInterceptor sut =
      new SerializationFailureRetryInterceptor();

  @Test
  void 直列化失敗が続いても上限内で成功すれば結果を返す() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    Object result =
        sut.retry(
            context(() -> {
              if (calls.incrementAndGet() < 3) {
                throw serializationFailure();
              }
              return "ok";
            }));

    assertEquals("ok", result);
    assertEquals(3, calls.get());
  }

  @Test
  void 上限回数を超えたら最後の例外を投げる() {
    AtomicInteger calls = new AtomicInteger();

    assertThrows(
        DataAccessException.class,
        () ->
            sut.retry(
                context(() -> {
                  calls.incrementAndGet();
                  throw serializationFailure();
                })));
    assertEquals(SerializationFailureRetryInterceptor.MAX_ATTEMPTS, calls.get());
  }

  @Test
  void 直列化失敗以外の例外は再試行しない() {
    AtomicInteger calls = new AtomicInteger();

    assertThrows(
        DataAccessException.class,
        () ->
            sut.retry(
                context(() -> {
                  calls.incrementAndGet();
                  throw new DataAccessException(new SQLException("duplicate", "23505"));
                })));
    assertEquals(1, calls.get());
  }

  @Test
  void 原因をたどって40001を判定する() {
    var wrapped = new RuntimeException(new IllegalStateException(serializationFailure()));
    assertTrue(SerializationFailureRetryInterceptor.isSerializationFailure(wrapped));
  }

  @Test
  void 抑制された例外の40001も判定する() {
    var e = new RuntimeException("commit failed");
    e.addSuppressed(new SQLException("restart transaction", "40001"));
    assertTrue(SerializationFailureRetryInterceptor.isSerializationFailure(e));
  }

  @Test
  void SQLExceptionを含まない例外は対象外() {
    assertFalse(
        SerializationFailureRetryInterceptor.isSerializationFailure(new IllegalStateException()));
  }

  private static DataAccessException serializationFailure() {
    return new DataAccessException(new SQLException("restart transaction", "40001"));
  }

  private static InvocationContext context(Callable<Object> body) {
    return new InvocationContext() {
      @Override
      public Object proceed() throws Exception {
        return body.call();
      }

      @Override
      public Method getMethod() {
        try {
          return Object.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
          throw new IllegalStateException(e);
        }
      }

      @Override
      public Object getTarget() {
        return null;
      }

      @Override
      public Object getTimer() {
        return null;
      }

      @Override
      public Constructor<?> getConstructor() {
        return null;
      }

      @Override
      public Object[] getParameters() {
        return new Object[0];
      }

      @Override
      public void setParameters(Object[] params) {}

      @Override
      public Map<String, Object> getContextData() {
        return Map.of();
      }
    };
  }
}
