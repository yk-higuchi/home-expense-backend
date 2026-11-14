package dev.higuchi.homeexpense.infrastructure.datasource.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.infrastructure.datasource.DataAccessException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.Transactional;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** インターセプターが @Transactional の外側で動き、試行ごとに新しいトランザクションになることを確認する */
@QuarkusTest
class RetryOnSerializationFailureIntegrationTest {

  @Inject Probe probe;

  @BeforeEach
  void setUp() {
    probe.reset();
  }

  @Test
  void 試行ごとに別のトランザクションで再実行される() {
    probe.failBefore(2);

    assertEquals("ok", probe.run());

    assertEquals(3, probe.transactionKeys().size());
    assertTrue(probe.transactionKeys().stream().allMatch(k -> k != null));
    assertEquals(3, new HashSet<>(probe.transactionKeys()).size());
  }

  @Test
  void 上限を超えたら例外が呼び出し元に伝わる() {
    probe.failBefore(Integer.MAX_VALUE);

    assertThrows(DataAccessException.class, probe::run);
    assertEquals(SerializationFailureRetryInterceptor.MAX_ATTEMPTS, probe.transactionKeys().size());
  }

  @ApplicationScoped
  @Transactional
  @RetryOnSerializationFailure
  public static class Probe {

    @Inject TransactionSynchronizationRegistry registry;

    final List<Object> transactionKeys = new ArrayList<>();
    int failuresBeforeSuccess;

    // 注入されるのはクライアントプロキシなので、状態はフィールドではなくメソッド経由で読み書きする
    public void reset() {
      transactionKeys.clear();
      failuresBeforeSuccess = 0;
    }

    public void failBefore(int count) {
      failuresBeforeSuccess = count;
    }

    public List<Object> transactionKeys() {
      return List.copyOf(transactionKeys);
    }

    public String run() {
      transactionKeys.add(registry.getTransactionKey());
      if (transactionKeys.size() <= failuresBeforeSuccess) {
        throw new DataAccessException(new SQLException("restart transaction", "40001"));
      }
      return "ok";
    }
  }
}
