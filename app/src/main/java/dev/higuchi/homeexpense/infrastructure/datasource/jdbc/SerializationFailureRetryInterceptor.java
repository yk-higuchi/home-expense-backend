package dev.higuchi.homeexpense.infrastructure.datasource.jdbc;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import org.jboss.logging.Logger;

/**
 * {@link RetryOnSerializationFailure} の実装。
 *
 * <p>{@link jakarta.transaction.Transactional} のインターセプター (PLATFORM_BEFORE + 200) より外側で動くよう優先度を
 * 小さくしている。これにより試行ごとに新しいトランザクションで実行される。
 */
@RetryOnSerializationFailure
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 100)
public class SerializationFailureRetryInterceptor {

  static final String SQLSTATE_SERIALIZATION_FAILURE = "40001";
  static final int MAX_ATTEMPTS = 3;
  private static final long BASE_BACKOFF_MILLIS = 50;

  private static final Logger LOG = Logger.getLogger(SerializationFailureRetryInterceptor.class);

  @AroundInvoke
  Object retry(InvocationContext context) throws Exception {
    for (int attempt = 1; ; attempt++) {
      try {
        return context.proceed();
      } catch (Exception e) {
        if (attempt >= MAX_ATTEMPTS || !isSerializationFailure(e)) {
          throw e;
        }
        LOG.warnv(
            "直列化失敗のためトランザクションを再試行します ({0}/{1}): {2}",
            attempt, MAX_ATTEMPTS, context.getMethod().toGenericString());
        sleep(backoffMillis(attempt));
      }
    }
  }

  static boolean isSerializationFailure(Throwable throwable) {
    for (Throwable t = throwable; t != null; t = t.getCause()) {
      if (t instanceof SQLException sql
          && SQLSTATE_SERIALIZATION_FAILURE.equals(sql.getSQLState())) {
        return true;
      }
      for (Throwable suppressed : t.getSuppressed()) {
        if (isSerializationFailure(suppressed)) {
          return true;
        }
      }
      if (t.getCause() == t) {
        break;
      }
    }
    return false;
  }

  /** 指数バックオフ + ジッター (50ms, 100ms, ... にそれぞれ最大同量のランダム時間を加える) */
  private static long backoffMillis(int attempt) {
    long base = BASE_BACKOFF_MILLIS << (attempt - 1);
    return base + ThreadLocalRandom.current().nextLong(base + 1);
  }

  private static void sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }
}
