package dev.higuchi.homeexpense.infrastructure.datasource.income;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.income.*;
import dev.higuchi.homeexpense.core.model.income.attribute.*;
import dev.higuchi.homeexpense.infrastructure.datasource.income.attribute.IncomeAttributeDataSource;
import dev.higuchi.homeexpense.query.Pagination;
import dev.higuchi.homeexpense.query.income.IncomeDetail;
import dev.higuchi.homeexpense.query.income.IncomeSearchCriteria;
import dev.higuchi.homeexpense.query.income.IncomeSearchResult;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class IncomeSearchResultDataSourceTest {

  @Inject IncomeSearchResultDataSource sut;

  @Inject IncomeDataSource incomeDataSource;

  @Inject IncomeAttributeDataSource incomeAttributeDataSource;

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
  void 全件検索で結果を取得できる() {
    IncomeAttribute attribute = registerAttribute("給与");
    registerIncome("4月給与", 300000, "2026-04-25", attribute);
    registerIncome("5月給与", 310000, "2026-05-25", attribute);

    Pagination pagination = new Pagination(1, 10);
    IncomeSearchCriteria criteria = new IncomeSearchCriteria(pagination, null);
    IncomeSearchResult result = sut.search(criteria);

    assertEquals(2, result.totalCount());
    assertEquals(2, result.list().size());
  }

  @Test
  void 年で絞り込み検索できる() {
    IncomeAttribute attribute = registerAttribute("給与2");
    registerIncome("2026年給与", 300000, "2026-04-25", attribute);
    registerIncome("2025年給与", 280000, "2025-04-25", attribute);

    Pagination pagination = new Pagination(1, 10);
    IncomeSearchCriteria criteria = new IncomeSearchCriteria(pagination, 2026);
    IncomeSearchResult result = sut.search(criteria);

    assertEquals(1, result.totalCount());
    assertEquals("2026年給与", result.list().getFirst().description());
  }

  @Test
  void ページネーションが正しく動作する() {
    IncomeAttribute attribute = registerAttribute("給与3");
    for (int i = 1; i <= 5; i++) {
      registerIncome("収入" + i, i * 10000, "2026-04-%02d".formatted(i), attribute);
    }

    Pagination pagination = new Pagination(1, 2);
    IncomeSearchCriteria criteria = new IncomeSearchCriteria(pagination, null);
    IncomeSearchResult result = sut.search(criteria);

    assertEquals(5, result.totalCount());
    assertEquals(2, result.list().size());
  }

  @Test
  void データが存在しない場合は空の結果が返る() {
    Pagination pagination = new Pagination(1, 10);
    IncomeSearchCriteria criteria = new IncomeSearchCriteria(pagination, null);
    IncomeSearchResult result = sut.search(criteria);

    assertEquals(0, result.totalCount());
    assertTrue(result.list().isEmpty());
  }

  @Test
  void 同じ日の収入は登録の新しい順で_登録日時も同じならIDの順に並ぶ() throws SQLException {
    IncomeAttribute attribute = registerAttribute("給与並び");
    String oldest =
        registerIncomeAt(
            "00000000-0000-0000-0000-000000000003", attribute, "2026-04-25 09:00:00+00");
    String newest =
        registerIncomeAt(
            "00000000-0000-0000-0000-000000000004", attribute, "2026-04-25 12:00:00+00");
    String tieB =
        registerIncomeAt(
            "00000000-0000-0000-0000-000000000002", attribute, "2026-04-25 10:00:00+00");
    String tieA =
        registerIncomeAt(
            "00000000-0000-0000-0000-000000000001", attribute, "2026-04-25 10:00:00+00");

    IncomeSearchResult result = sut.search(new IncomeSearchCriteria(new Pagination(1, 10), null));

    assertEquals(
        List.of(newest, tieA, tieB, oldest), result.list().stream().map(IncomeDetail::id).toList());
  }

  @Test
  void ページを順に取得すると同じ日の収入もちょうど1回ずつ出る() throws SQLException {
    IncomeAttribute attribute = registerAttribute("給与ページ");
    List<String> registered = new ArrayList<>();
    for (int i = 0; i < 17; i++) {
      registered.add(
          registerIncomeAt(UUID.randomUUID().toString(), attribute, "2026-03-15 10:00:00+00"));
    }

    List<String> seen = new ArrayList<>();
    for (int page = 1; page <= 4; page++) {
      sut.search(new IncomeSearchCriteria(new Pagination(page, 5), null))
          .list()
          .forEach(i -> seen.add(i.id()));
    }

    assertEquals(17, seen.size());
    assertEquals(new HashSet<>(registered), new HashSet<>(seen));
    assertEquals(registered.stream().sorted().toList(), seen);
  }

  private IncomeAttribute registerAttribute(String name) {
    IncomeAttribute attribute = IncomeAttribute.create(new IncomeAttributeName(name));
    incomeAttributeDataSource.register(attribute);
    return attribute;
  }

  private void registerIncome(
      String description, int amount, String receiveDate, IncomeAttribute attribute) {
    Income income =
        new Income(
            new IncomeIdentifier(UUID.randomUUID().toString()),
            new Description(description),
            new Amount(amount),
            new ReceiveDate(LocalDate.parse(receiveDate)),
            attribute.incomeAttributeIdentifier());
    incomeDataSource.register(income);
  }

  private String registerIncomeAt(String id, IncomeAttribute attribute, String createdAt)
      throws SQLException {
    incomeDataSource.register(
        new Income(
            new IncomeIdentifier(id),
            new Description("並び" + id),
            new Amount(100),
            new ReceiveDate(LocalDate.parse(createdAt.substring(0, 10))),
            attribute.incomeAttributeIdentifier()));
    try (Connection conn = dataSource.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "UPDATE expense.income SET created_at = ?::TIMESTAMPTZ WHERE id = ?")) {
      ps.setString(1, createdAt);
      ps.setString(2, id);
      ps.executeUpdate();
    }
    return id;
  }
}
