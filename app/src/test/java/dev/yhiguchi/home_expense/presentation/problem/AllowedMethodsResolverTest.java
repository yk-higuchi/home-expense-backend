package dev.yhiguchi.home_expense.presentation.problem;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AllowedMethodsResolverTest {

  @Inject BeanManager beanManager;

  private final AllowedMethodsResolver sut = new AllowedMethodsResolver();

  @Test
  void コレクションのパスはGETとPOSTを許可する() {
    assertEquals(Optional.of("GET, HEAD, OPTIONS, POST"), sut.allowFor("/v1/incomes"));
  }

  @Test
  void 個別リソースのパスはGET_PUT_DELETEを許可する() {
    assertEquals(
        Optional.of("DELETE, GET, HEAD, OPTIONS, PUT"),
        sut.allowFor("/v1/incomes/3f1c1d9e-0000-0000-0000-000000000000"));
  }

  @Test
  void 末尾スラッシュは無視する() {
    assertEquals(sut.allowFor("/v1/incomes"), sut.allowFor("/v1/incomes/"));
  }

  @Test
  void 該当しないパスは空() {
    assertEquals(Optional.empty(), sut.allowFor("/v1/unknown"));
  }

  @Test
  void すべてのJAXRSリソースが登録されている() {
    Set<Class<?>> resources =
        beanManager.getBeans(Object.class).stream()
            .map(bean -> bean.getBeanClass())
            .filter(c -> c.isAnnotationPresent(Path.class))
            .filter(c -> c.getPackageName().startsWith("dev.yhiguchi.home_expense"))
            .collect(Collectors.toSet());

    assertFalse(resources.isEmpty());
    assertEquals(resources, new HashSet<>(AllowedMethodsResolver.RESOURCES));
  }
}
