package dev.higuchi.homeexpense.infrastructure.datasource.income.attribute;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.ConcurrentUpdateException;
import dev.higuchi.homeexpense.testing.TestDigests;
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
    sut.update(updated, ExpectedDigest.of(attribute.digest()));

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
        ConcurrentUpdateException.class,
        () -> sut.update(modified, ExpectedDigest.of(TestDigests.STALE)));
  }

  @Test
  void 複数のバージョンのいずれかと一致すれば更新できる() {
    IncomeAttribute attribute = createAttribute("年金");
    sut.register(attribute);

    sut.update(
        new IncomeAttribute(attribute.incomeAttributeIdentifier(), new IncomeAttributeName("年金2")),
        ExpectedDigest.oneOf(Set.of(TestDigests.STALE, attribute.digest())));

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
        ExpectedDigest.unconditional());

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
                ExpectedDigest.oneOf(Set.of())));
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

    assertDoesNotThrow(() -> sut.verifyDigest(attribute, ExpectedDigest.of(attribute.digest())));
    assertDoesNotThrow(() -> sut.verifyDigest(attribute, ExpectedDigest.unconditional()));
    assertThrows(
        ConcurrentUpdateException.class,
        () -> sut.verifyDigest(attribute, ExpectedDigest.of(TestDigests.STALE)));
  }

  @Test
  void 収入属性を削除できる() {
    IncomeAttribute attribute = createAttribute("その他");
    sut.register(attribute);

    sut.delete(attribute, ExpectedDigest.of(attribute.digest()));
    assertTrue(sut.find(attribute.incomeAttributeIdentifier()).isEmpty());
  }

  private IncomeAttribute createAttribute(String name) {
    return IncomeAttribute.create(new IncomeAttributeName(name));
  }

  @Test
  void バージョンが一致しない収入属性は削除せず例外をスローする() {
    IncomeAttribute attribute = createAttribute("その他");
    sut.register(attribute);

    assertThrows(
        ConcurrentUpdateException.class,
        () -> sut.delete(attribute, ExpectedDigest.of(TestDigests.STALE)));
    assertTrue(sut.find(attribute.incomeAttributeIdentifier()).isPresent());
  }
}
