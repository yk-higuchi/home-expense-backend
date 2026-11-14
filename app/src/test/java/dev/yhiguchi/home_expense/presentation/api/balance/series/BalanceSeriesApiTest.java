package dev.yhiguchi.home_expense.presentation.api.balance.series;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BalanceSeriesApiTest {

  private static final String PATH = "/v1/balance-series";

  @Test
  void GET_月粒度の収支時系列を取得できる() {
    given()
        .queryParam("from", "2026-01-01")
        .queryParam("to", "2026-04-30")
        .queryParam("granularity", "month")
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("granularity", equalTo("month"))
        .body("items", hasSize(4))
        .body("items[0].period.from", equalTo("2026-01-01"))
        .body("items[0].period.to", equalTo("2026-01-31"))
        .body("items[0].income_total", notNullValue())
        .body("items[0].fixed_expense_total", notNullValue())
        .body("items[0].variable_expense_total", notNullValue())
        .body("items[0].expense_total", notNullValue())
        .body("items[0].net", notNullValue());
  }

  @Test
  void GET_granularity不正値で400() {
    given()
        .queryParam("from", "2026-01-01")
        .queryParam("to", "2026-04-30")
        .queryParam("granularity", "fortnight")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void GET_granularity大文字でも受け入れる() {
    given()
        .queryParam("from", "2026-01-01")
        .queryParam("to", "2026-04-30")
        .queryParam("granularity", "MONTH")
        .when()
        .get(PATH)
        .then()
        .statusCode(200);
  }

  @Test
  void GET_必須パラメータ欠落で400() {
    given().when().get(PATH).then().statusCode(400);
  }

  @Test
  void GET_from_to順序違反で400() {
    given()
        .queryParam("from", "2026-04-01")
        .queryParam("to", "2026-01-01")
        .queryParam("granularity", "month")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }

  @Test
  void GET_期間が長すぎて366ポイント超過なら400() {
    given()
        .queryParam("from", "2024-01-01")
        .queryParam("to", "2026-12-31")
        .queryParam("granularity", "day")
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }
}
