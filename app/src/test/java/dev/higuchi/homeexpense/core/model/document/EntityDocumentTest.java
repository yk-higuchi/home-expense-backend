package dev.higuchi.homeexpense.core.model.document;

import static org.junit.jupiter.api.Assertions.*;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.expense.Expense;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.ExpenseIdentifier;
import dev.higuchi.homeexpense.core.model.expense.PaymentDate;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttribute;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import dev.higuchi.homeexpense.core.model.income.Income;
import dev.higuchi.homeexpense.core.model.income.IncomeIdentifier;
import dev.higuchi.homeexpense.core.model.income.ReceiveDate;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeName;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EntityDocumentTest {

  @Test
  void 支出のドキュメントはPUTの本文と同じ項目() {
    Expense expense =
        new Expense(
            new ExpenseIdentifier("exp-1"),
            new Description("ランチ \"特盛\""),
            new Amount(1000),
            new PaymentDate(LocalDate.of(2026, 4, 1)),
            new ExpenseAttributeIdentifier("attr-1"));
    assertEquals(
        DocumentDigest.ofCanonicalJson(
            "{\"amount\":1000,\"attribute_id\":\"attr-1\","
                + "\"description\":\"ランチ \\\"特盛\\\"\",\"payment_date\":\"2026-04-01\"}"),
        expense.digest());
  }

  @Test
  void 収入のドキュメントはPUTの本文と同じ項目() {
    Income income =
        new Income(
            new IncomeIdentifier("inc-1"),
            new Description("4月給与"),
            new Amount(300000),
            new ReceiveDate(LocalDate.of(2026, 4, 25)),
            new IncomeAttributeIdentifier("attr-1"));
    assertEquals(
        DocumentDigest.ofCanonicalJson(
            "{\"amount\":300000,\"attribute_id\":\"attr-1\","
                + "\"description\":\"4月給与\",\"receive_date\":\"2026-04-25\"}"),
        income.digest());
  }

  @Test
  void 支出属性のドキュメントは区分と名前() {
    ExpenseAttribute attribute =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    assertEquals(
        DocumentDigest.ofCanonicalJson("{\"category\":\"変動費\",\"name\":\"食費\"}"),
        attribute.digest());
  }

  @Test
  void マイグレーションのSQLで計算した値と一致する() {
    ExpenseAttribute attribute =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-2"),
            new ExpenseAttributeName("a\\b\b\f\u0001\u001f/<>&\u007f\u2028\ud83d\ude00"),
            ExpenseCategory.固定費);
    assertEquals("wU17K8Pblt5ittwQiQmMwO6lGApABgvvC-PXBOtRSpg", attribute.digest().value());
  }

  @Test
  void 収入属性のドキュメントは名前() {
    IncomeAttribute attribute =
        new IncomeAttribute(new IncomeAttributeIdentifier("attr-1"), new IncomeAttributeName("給与"));
    assertEquals(DocumentDigest.ofCanonicalJson("{\"name\":\"給与\"}"), attribute.digest());
  }

  @Test
  void IDが違っても内容が同じなら同じダイジェスト() {
    ExpenseAttribute first =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-1"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    ExpenseAttribute second =
        new ExpenseAttribute(
            new ExpenseAttributeIdentifier("attr-2"),
            new ExpenseAttributeName("食費"),
            ExpenseCategory.変動費);
    assertEquals(first.digest(), second.digest());
  }
}
