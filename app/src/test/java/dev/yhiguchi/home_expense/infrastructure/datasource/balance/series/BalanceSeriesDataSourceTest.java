package dev.yhiguchi.home_expense.infrastructure.datasource.balance.series;

import static org.junit.jupiter.api.Assertions.*;

import dev.yhiguchi.home_expense.domain.model.Amount;
import dev.yhiguchi.home_expense.domain.model.Description;
import dev.yhiguchi.home_expense.domain.model.expense.Expense;
import dev.yhiguchi.home_expense.domain.model.expense.ExpenseCategory;
import dev.yhiguchi.home_expense.domain.model.expense.ExpenseIdentifier;
import dev.yhiguchi.home_expense.domain.model.expense.PaymentDate;
import dev.yhiguchi.home_expense.domain.model.expense.attribute.ExpenseAttribute;
import dev.yhiguchi.home_expense.domain.model.expense.attribute.ExpenseAttributeName;
import dev.yhiguchi.home_expense.domain.model.income.Income;
import dev.yhiguchi.home_expense.domain.model.income.IncomeIdentifier;
import dev.yhiguchi.home_expense.domain.model.income.ReceiveDate;
import dev.yhiguchi.home_expense.domain.model.income.attribute.IncomeAttribute;
import dev.yhiguchi.home_expense.domain.model.income.attribute.IncomeAttributeName;
import dev.yhiguchi.home_expense.infrastructure.datasource.expense.ExpenseDataSource;
import dev.yhiguchi.home_expense.infrastructure.datasource.expense.attribute.ExpenseAttributeDataSource;
import dev.yhiguchi.home_expense.infrastructure.datasource.income.IncomeDataSource;
import dev.yhiguchi.home_expense.infrastructure.datasource.income.attribute.IncomeAttributeDataSource;
import dev.yhiguchi.home_expense.query.Granularity;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeries;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesCriteria;
import dev.yhiguchi.home_expense.query.balance.series.BalanceSeriesPoint;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BalanceSeriesDataSourceTest {

  @Inject BalanceSeriesDataSource sut;

  @Inject ExpenseDataSource expenseDataSource;

  @Inject ExpenseAttributeDataSource expenseAttributeDataSource;

  @Inject IncomeDataSource incomeDataSource;

  @Inject IncomeAttributeDataSource incomeAttributeDataSource;

  @Inject DataSource dataSource;

  @BeforeEach
  void setUp() throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      for (String table :
          List.of(
              "expense.expense",
              "expense.attribute",
              "expense.income",
              "expense.income_attribute")) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table)) {
          ps.executeUpdate();
        }
      }
    }
  }

  @Test
  void MONTH粒度_データなしでも各月が0埋めで返る() {
    BalanceSeries result = sut.search(monthly("2026-01-01", "2026-04-30"));

    assertEquals(Granularity.MONTH, result.granularity());
    assertEquals(4, result.items().size());
    BalanceSeriesPoint first = result.items().get(0);
    assertEquals(LocalDate.of(2026, 1, 1), first.from());
    assertEquals(LocalDate.of(2026, 1, 31), first.to());
    assertEquals(0L, first.incomeTotal());
    assertEquals(0L, first.expenseTotal());
    assertEquals(0L, first.net());
  }

  @Test
  void MONTH粒度_1年分で12点が1月から順に返る() {
    BalanceSeries result = sut.search(monthly("2026-01-01", "2026-12-31"));

    assertEquals(12, result.items().size());
    assertEquals(LocalDate.of(2026, 1, 1), result.items().get(0).from());
    assertEquals(LocalDate.of(2026, 12, 1), result.items().get(11).from());
    assertEquals(LocalDate.of(2026, 12, 31), result.items().get(11).to());
  }

  @Test
  void MONTH粒度_収入と固定費と変動費が振り分けられる() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute rent = registerExpenseAttribute("家賃", ExpenseCategory.固定費);
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerIncome(300000, "2026-04-25", salary);
    registerExpense(80000, "2026-04-01", rent);
    registerExpense(1000, "2026-04-10", food);
    registerExpense(3000, "2026-04-15", food);

    BalanceSeriesPoint april = sut.search(monthly("2026-04-01", "2026-04-30")).items().get(0);

    assertEquals(300000L, april.incomeTotal());
    assertEquals(80000L, april.fixedExpenseTotal());
    assertEquals(4000L, april.variableExpenseTotal());
    assertEquals(84000L, april.expenseTotal());
    assertEquals(216000L, april.net());
  }

  @Test
  void MONTH粒度_同じ月に収入と経費が複数件あっても合計が水増しされない() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute rent = registerExpenseAttribute("家賃", ExpenseCategory.固定費);
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerIncome(100000, "2026-05-10", salary);
    registerIncome(200000, "2026-05-25", salary);
    registerExpense(80000, "2026-05-01", rent);
    registerExpense(1000, "2026-05-01", food);
    registerExpense(2000, "2026-05-02", food);
    registerExpense(3000, "2026-05-03", food);

    BalanceSeriesPoint may = sut.search(monthly("2026-05-01", "2026-05-31")).items().get(0);

    assertEquals(300000L, may.incomeTotal());
    assertEquals(80000L, may.fixedExpenseTotal());
    assertEquals(6000L, may.variableExpenseTotal());
  }

  @Test
  void MONTH粒度_収入のみの月と経費のみの月() {
    IncomeAttribute bonus = registerIncomeAttribute("賞与");
    ExpenseAttribute insurance = registerExpenseAttribute("保険", ExpenseCategory.固定費);
    registerIncome(500000, "2026-06-30", bonus);
    registerExpense(120000, "2026-07-01", insurance);

    BalanceSeries result = sut.search(monthly("2026-06-01", "2026-07-31"));

    BalanceSeriesPoint june = result.items().get(0);
    assertEquals(500000L, june.incomeTotal());
    assertEquals(0L, june.expenseTotal());
    assertEquals(500000L, june.net());

    BalanceSeriesPoint july = result.items().get(1);
    assertEquals(0L, july.incomeTotal());
    assertEquals(120000L, july.fixedExpenseTotal());
    assertEquals(-120000L, july.net());
  }

  @Test
  void MONTH粒度_期間外のデータは含まれない() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerIncome(1, "2026-03-31", salary);
    registerIncome(10, "2026-04-01", salary);
    registerIncome(100, "2026-04-30", salary);
    registerIncome(1000, "2026-05-01", salary);
    registerExpense(1, "2026-03-31", food);
    registerExpense(10, "2026-04-01", food);
    registerExpense(100, "2026-04-30", food);
    registerExpense(1000, "2026-05-01", food);

    BalanceSeries result = sut.search(monthly("2026-04-01", "2026-04-30"));

    assertEquals(1, result.items().size());
    assertEquals(110L, result.items().get(0).incomeTotal());
    assertEquals(110L, result.items().get(0).variableExpenseTotal());
  }

  @Test
  void YEAR粒度_2年分が返る() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute fee = registerExpenseAttribute("年会費", ExpenseCategory.固定費);
    registerIncome(5000000, "2025-06-25", salary);
    registerIncome(5500000, "2026-06-25", salary);
    registerExpense(10000, "2025-06-01", fee);
    registerExpense(12000, "2026-06-01", fee);

    BalanceSeries result =
        sut.search(
            new BalanceSeriesCriteria(
                LocalDate.of(2025, 1, 1), LocalDate.of(2026, 12, 31), Granularity.YEAR));

    assertEquals(2, result.items().size());
    assertEquals(LocalDate.of(2025, 1, 1), result.items().get(0).from());
    assertEquals(LocalDate.of(2025, 12, 31), result.items().get(0).to());
    assertEquals(5000000L, result.items().get(0).incomeTotal());
    assertEquals(10000L, result.items().get(0).fixedExpenseTotal());
    assertEquals(5500000L, result.items().get(1).incomeTotal());
    assertEquals(12000L, result.items().get(1).fixedExpenseTotal());
  }

  @Test
  void QUARTER粒度_1年分で4四半期が返る() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerIncome(300000, "2026-06-30", salary);
    registerExpense(1000, "2026-04-01", food);
    registerExpense(2000, "2026-12-31", food);

    BalanceSeries result =
        sut.search(
            new BalanceSeriesCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), Granularity.QUARTER));

    assertEquals(4, result.items().size());
    assertEquals(LocalDate.of(2026, 4, 1), result.items().get(1).from());
    assertEquals(LocalDate.of(2026, 6, 30), result.items().get(1).to());
    assertEquals(300000L, result.items().get(1).incomeTotal());
    assertEquals(1000L, result.items().get(1).variableExpenseTotal());
    assertEquals(LocalDate.of(2026, 10, 1), result.items().get(3).from());
    assertEquals(LocalDate.of(2026, 12, 31), result.items().get(3).to());
    assertEquals(2000L, result.items().get(3).variableExpenseTotal());
  }

  @Test
  void WEEK粒度_月曜始まりの週で集計される() {
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerExpense(1000, "2026-05-03", food); // 日曜 → 4/27 の週
    registerExpense(2000, "2026-05-04", food); // 月曜 → 5/4 の週
    registerExpense(3000, "2026-05-10", food); // 日曜 → 5/4 の週

    BalanceSeries result =
        sut.search(
            new BalanceSeriesCriteria(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 10), Granularity.WEEK));

    assertEquals(2, result.items().size());
    assertEquals(LocalDate.of(2026, 4, 27), result.items().get(0).from());
    assertEquals(LocalDate.of(2026, 5, 3), result.items().get(0).to());
    assertEquals(1000L, result.items().get(0).variableExpenseTotal());
    assertEquals(LocalDate.of(2026, 5, 4), result.items().get(1).from());
    assertEquals(LocalDate.of(2026, 5, 10), result.items().get(1).to());
    assertEquals(5000L, result.items().get(1).variableExpenseTotal());
  }

  @Test
  void DAY粒度_3日分() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerExpense(500, "2026-04-01", food);
    registerIncome(20000, "2026-04-02", salary);
    registerExpense(1500, "2026-04-03", food);

    BalanceSeries result =
        sut.search(
            new BalanceSeriesCriteria(
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3), Granularity.DAY));

    assertEquals(3, result.items().size());
    assertEquals(500L, result.items().get(0).variableExpenseTotal());
    assertEquals(0L, result.items().get(0).incomeTotal());
    assertEquals(20000L, result.items().get(1).incomeTotal());
    assertEquals(0L, result.items().get(1).variableExpenseTotal());
    assertEquals(1500L, result.items().get(2).variableExpenseTotal());
  }

  private static BalanceSeriesCriteria monthly(String from, String to) {
    return new BalanceSeriesCriteria(LocalDate.parse(from), LocalDate.parse(to), Granularity.MONTH);
  }

  private IncomeAttribute registerIncomeAttribute(String name) {
    IncomeAttribute attribute = IncomeAttribute.create(new IncomeAttributeName(name));
    incomeAttributeDataSource.register(attribute);
    return attribute;
  }

  private void registerIncome(int amount, String receiveDate, IncomeAttribute attribute) {
    Income income =
        new Income(
            new IncomeIdentifier(UUID.randomUUID().toString()),
            new Description("test"),
            new Amount(amount),
            new ReceiveDate(LocalDate.parse(receiveDate)),
            attribute.incomeAttributeIdentifier());
    incomeDataSource.register(income);
  }

  private ExpenseAttribute registerExpenseAttribute(String name, ExpenseCategory category) {
    ExpenseAttribute attribute = ExpenseAttribute.create(new ExpenseAttributeName(name), category);
    expenseAttributeDataSource.register(attribute);
    return attribute;
  }

  private void registerExpense(int amount, String paymentDate, ExpenseAttribute attribute) {
    Expense expense =
        new Expense(
            new ExpenseIdentifier(UUID.randomUUID().toString()),
            new Description("test"),
            new Amount(amount),
            new PaymentDate(LocalDate.parse(paymentDate)),
            attribute.expenseAttributeIdentifier());
    expenseDataSource.register(expense);
  }
}
