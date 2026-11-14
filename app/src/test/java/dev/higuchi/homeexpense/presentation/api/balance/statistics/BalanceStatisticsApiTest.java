package dev.higuchi.homeexpense.presentation.api.balance.statistics;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class BalanceStatisticsApiTest {

  private static final String PATH = "/v1/balance-statistics";

  @Test
  void GET_年のみ指定で年間の収支統計を取得できる() {
    given()
        .queryParam("year", 2026)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("period.from", equalTo("2026-01-01"))
        .body("period.to", equalTo("2026-12-31"))
        .body("income.total", notNullValue())
        .body("income.breakdown", notNullValue())
        .body("fixed_expense.total", notNullValue())
        .body("fixed_expense.breakdown", notNullValue())
        .body("variable_expense.total", notNullValue())
        .body("variable_expense.breakdown", notNullValue())
        .body("expense_total", notNullValue())
        .body("net", notNullValue());
  }

  @Test
  void GET_年月指定で月次の収支統計を取得できる() {
    given()
        .queryParam("year", 2026)
        .queryParam("month", 4)
        .when()
        .get(PATH)
        .then()
        .statusCode(200)
        .body("period.from", equalTo("2026-04-01"))
        .body("period.to", equalTo("2026-04-30"));
  }

  @Test
  void GET_year欠落で400() {
    given().queryParam("month", 4).when().get(PATH).then().statusCode(400);
  }

  @Test
  void GET_yearが0なら400() {
    given().queryParam("year", 0).when().get(PATH).then().statusCode(400);
  }

  @Test
  void GET_yearが2201なら400() {
    given().queryParam("year", 2201).when().get(PATH).then().statusCode(400);
  }

  @Test
  void GET_monthが0なら400() {
    given().queryParam("year", 2026).queryParam("month", 0).when().get(PATH).then().statusCode(400);
  }

  @Test
  void GET_monthが13なら400() {
    given()
        .queryParam("year", 2026)
        .queryParam("month", 13)
        .when()
        .get(PATH)
        .then()
        .statusCode(400);
  }
}
