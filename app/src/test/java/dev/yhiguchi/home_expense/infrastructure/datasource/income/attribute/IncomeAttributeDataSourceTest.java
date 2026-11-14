package dev.yhiguchi.home_expense.infrastructure.datasource.income.attribute;

import static org.junit.jupiter.api.Assertions.*;

import dev.yhiguchi.home_expense.domain.model.ExpectedVersion;
import dev.yhiguchi.home_expense.domain.model.income.attribute.*;
import dev.yhiguchi.home_expense.infrastructure.datasource.ConcurrentUpdateException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IncomeAttributeDataSourceTest {

  private static final long INITIAL_VERSION = 1L;

  @Inject IncomeAttributeDataSource sut;

  @Inject DataSource dataSource;

  @BeforeEach
  void setUp() throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expense.income")) {
        ps.executeUpdate();
      }
      try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expense.income_attribute")) {
        ps.executeUpdate();
      }
    }
  }

  @Test
  void 登録した収入属性を取得できる() {
    IncomeAttribute attribute = createAttribute("給与");
    sut.register(attribute);

    IncomeAttribute result = sut.find(attribute.incomeAttributeIdentifier()).orElseThrow();
    assertEquals(attribute.incomeAttributeIdentifier(), result.incomeAttributeIdentifier());
    assertEquals("給与", result.incomeAttributeName().value());
  }

  @Test
  void 存在しないIDで取得すると空のOptionalを返す() {
    IncomeAttributeIdentifier unknownId =
        new IncomeAttributeIdentifier(UUID.randomUUID().toString());
    assertTrue(sut.find(unknownId).isEmpty());
  }

  @Test
  void 名前で存在確認できる() {
    IncomeAttribute attribute = createAttribute("賞与");
    sut.register(attribute);

    assertTrue(sut.existsByName(new IncomeAttributeName("賞与")));
  }

  @Test
  void 存在しない名前の場合falseを返す() {
    assertFalse(sut.existsByName(new IncomeAttributeName("存在しない")));
  }

  @Test
  void 収入属性を更新できる() {
    IncomeAttribute attribute = createAttribute("副業");
    sut.register(attribute);

    IncomeAttribute fetched = sut.find(attribute.incomeAttributeIdentifier()).orElseThrow();
    IncomeAttribute updated =
        new IncomeAttribute(fetched.incomeAttributeIdentifier(), new IncomeAttributeName("副業収入"));
    sut.update(updated, ExpectedVersion.of(INITIAL_VERSION));

    IncomeAttribute result = sut.find(attribute.incomeAttributeIdentifier()).orElseThrow();
    assertEquals("副業収入", result.incomeAttributeName().value());
  }

  @Test
  void バージョン不一致で更新すると楽観ロック例外が発生する() {
    IncomeAttribute attribute = createAttribute("配当");
    sut.register(attribute);

    IncomeAttribute modified =
        new IncomeAttribute(attribute.incomeAttributeIdentifier(), new IncomeAttributeName("配当更新"));
    assertThrows(
        ConcurrentUpdateException.class, () -> sut.update(modified, ExpectedVersion.of(999L)));
  }

  @Test
  void 複数のバージョンのいずれかと一致すれば更新できる() {
    IncomeAttribute attribute = createAttribute("年金");
    sut.register(attribute);

    sut.update(
        new IncomeAttribute(attribute.incomeAttributeIdentifier(), new IncomeAttributeName("年金2")),
        ExpectedVersion.oneOf(Set.of(5L, INITIAL_VERSION, 9L)));

    assertEquals(
        "年金2",
        sut.find(attribute.incomeAttributeIdentifier())
            .orElseThrow()
            .incomeAttributeName()
            .value());
  }

  @Test
  void バージョンを問わない指定なら更新できる() {
    IncomeAttribute attribute = createAttribute("利子");
    sut.register(attribute);

    sut.update(
        new IncomeAttribute(attribute.incomeAttributeIdentifier(), new IncomeAttributeName("利子2")),
        ExpectedVersion.unconditional());

    assertEquals(
        "利子2",
        sut.find(attribute.incomeAttributeIdentifier())
            .orElseThrow()
            .incomeAttributeName()
            .value());
  }

  @Test
  void 一致し得る候補がなければ更新せず楽観ロック例外() {
    IncomeAttribute attribute = createAttribute("謝礼");
    sut.register(attribute);

    assertThrows(
        ConcurrentUpdateException.class,
        () ->
            sut.update(
                new IncomeAttribute(
                    attribute.incomeAttributeIdentifier(), new IncomeAttributeName("謝礼2")),
                ExpectedVersion.oneOf(Set.of())));
    assertEquals(
        "謝礼",
        sut.find(attribute.incomeAttributeIdentifier())
            .orElseThrow()
            .incomeAttributeName()
            .value());
  }

  @Test
  void バージョン検証_一致すれば例外なし_不一致なら楽観ロック例外() {
    IncomeAttribute attribute = createAttribute("還付");
    sut.register(attribute);

    assertDoesNotThrow(() -> sut.verifyVersion(attribute, ExpectedVersion.of(INITIAL_VERSION)));
    assertDoesNotThrow(() -> sut.verifyVersion(attribute, ExpectedVersion.unconditional()));
    assertThrows(
        ConcurrentUpdateException.class,
        () -> sut.verifyVersion(attribute, ExpectedVersion.of(2L)));
  }

  @Test
  void 収入属性を削除できる() {
    IncomeAttribute attribute = createAttribute("その他");
    sut.register(attribute);

    sut.delete(attribute);
    assertTrue(sut.find(attribute.incomeAttributeIdentifier()).isEmpty());
  }

  private IncomeAttribute createAttribute(String name) {
    return IncomeAttribute.create(new IncomeAttributeName(name));
  }
}
