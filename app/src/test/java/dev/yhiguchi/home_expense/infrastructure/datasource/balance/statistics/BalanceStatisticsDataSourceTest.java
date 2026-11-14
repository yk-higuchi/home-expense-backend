package dev.yhiguchi.home_expense.infrastructure.datasource.balance.statistics;

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
import dev.yhiguchi.home_expense.query.balance.statistics.AttributeAmount;
import dev.yhiguchi.home_expense.query.balance.statistics.BalanceStatistics;
import dev.yhiguchi.home_expense.query.balance.statistics.BalanceStatisticsCriteria;
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
class BalanceStatisticsDataSourceTest {

  @Inject BalanceStatisticsDataSource sut;

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
  void 月指定_収入と固定費と変動費が集計される() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute rent = registerExpenseAttribute("家賃", ExpenseCategory.固定費);
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerIncome(300000, "2026-04-25", salary);
    registerExpense(80000, "2026-04-01", rent);
    registerExpense(1000, "2026-04-10", food);
    registerExpense(3000, "2026-04-10", food);

    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2026, 4));

    assertEquals(LocalDate.of(2026, 4, 1), result.from());
    assertEquals(LocalDate.of(2026, 4, 30), result.to());
    assertEquals(300000L, result.income().total());
    assertEquals(80000L, result.fixedExpense().total());
    assertEquals(4000L, result.variableExpense().total());
    assertEquals(84000L, result.expenseTotal());
    assertEquals(216000L, result.net());
  }

  @Test
  void データがない期間はすべて0で内訳は空() {
    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2025, 1));

    assertEquals(0L, result.income().total());
    assertEquals(0L, result.expenseTotal());
    assertEquals(0L, result.net());
    assertTrue(result.income().amounts().isEmpty());
    assertTrue(result.fixedExpense().amounts().isEmpty());
    assertTrue(result.variableExpense().amounts().isEmpty());
  }

  @Test
  void 月指定_月の境界_前月末日と翌月初日は含まれない() {
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

    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2026, 4));

    assertEquals(110L, result.income().total());
    assertEquals(110L, result.variableExpense().total());
  }

  @Test
  void 年指定_年の境界_前年12月31日と翌年1月1日は含まれない() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    ExpenseAttribute rent = registerExpenseAttribute("家賃", ExpenseCategory.固定費);
    registerIncome(1, "2025-12-31", salary);
    registerIncome(10, "2026-01-01", salary);
    registerIncome(100, "2026-12-31", salary);
    registerIncome(1000, "2027-01-01", salary);
    registerExpense(1, "2025-12-31", rent);
    registerExpense(10, "2026-01-01", rent);
    registerExpense(100, "2026-12-31", rent);
    registerExpense(1000, "2027-01-01", rent);

    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2026, null));

    assertEquals(LocalDate.of(2026, 1, 1), result.from());
    assertEquals(LocalDate.of(2026, 12, 31), result.to());
    assertEquals(110L, result.income().total());
    assertEquals(110L, result.fixedExpense().total());
  }

  @Test
  void 収入の内訳は属性ごとの合計を金額の降順で返す() {
    IncomeAttribute side = registerIncomeAttribute("副業");
    IncomeAttribute salary = registerIncomeAttribute("給与");
    registerIncome(50000, "2026-03-10", side);
    registerIncome(300000, "2026-01-25", salary);
    registerIncome(300000, "2026-02-25", salary);

    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2026, null));

    assertEquals(
        List.of(
            new AttributeAmount(salary.incomeAttributeIdentifier().value(), "給与", 600000),
            new AttributeAmount(side.incomeAttributeIdentifier().value(), "副業", 50000)),
        result.income().amounts());
  }

  @Test
  void 経費の内訳は固定費と変動費に分かれて金額の降順で返る() {
    ExpenseAttribute insurance = registerExpenseAttribute("保険", ExpenseCategory.固定費);
    ExpenseAttribute rent = registerExpenseAttribute("家賃", ExpenseCategory.固定費);
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerExpense(10000, "2026-01-05", insurance);
    registerExpense(80000, "2026-01-01", rent);
    registerExpense(80000, "2026-02-01", rent);
    registerExpense(3000, "2026-01-10", food);

    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2026, null));

    assertEquals(
        List.of(
            new AttributeAmount(rent.expenseAttributeIdentifier().value(), "家賃", 160000),
            new AttributeAmount(insurance.expenseAttributeIdentifier().value(), "保険", 10000)),
        result.fixedExpense().amounts());
    assertEquals(
        List.of(new AttributeAmount(food.expenseAttributeIdentifier().value(), "食費", 3000)),
        result.variableExpense().amounts());
  }

  @Test
  void 対象期間にデータがない属性は内訳に含まれない() {
    IncomeAttribute salary = registerIncomeAttribute("給与");
    registerIncomeAttribute("未使用の収入属性");
    ExpenseAttribute food = registerExpenseAttribute("食費", ExpenseCategory.変動費);
    registerExpenseAttribute("未使用の経費属性", ExpenseCategory.固定費);
    registerIncome(300000, "2026-04-25", salary);
    registerExpense(3000, "2026-05-10", food);

    BalanceStatistics result = sut.search(new BalanceStatisticsCriteria(2026, 4));

    assertEquals(1, result.income().amounts().size());
    assertTrue(result.fixedExpense().amounts().isEmpty());
    assertTrue(result.variableExpense().amounts().isEmpty());
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
