package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

@QuarkusTest
class InvalidQueryParameterApiTest {

  @Test
  void 整数パラメータに数値以外_balance_statistics() {
    assertBadRequest(
        given().queryParam("year", "abc").when().get("/v1/balance-statistics").then(),
        "yearは整数を指定してください");
  }

  @Test
  void 整数パラメータに数値以外_expenses() {
    assertBadRequest(
        given().queryParam("year", "abc").when().get("/v1/expenses").then(), "yearは整数を指定してください");
  }

  @Test
  void 整数パラメータに数値以外_incomes() {
    assertBadRequest(
        given().queryParam("year", "abc").when().get("/v1/incomes").then(), "yearは整数を指定してください");
  }

  @Test
  void 整数パラメータに数値以外_page() {
    assertBadRequest(
        given().queryParam("page", "x").when().get("/v1/incomes").then(), "pageは整数を指定してください");
  }

  @Test
  void 整数パラメータの範囲外_int上限超過() {
    assertBadRequest(
        given().queryParam("per_page", "99999999999").when().get("/v1/incomes").then(),
        "per_pageは整数を指定してください");
  }

  @Test
  void 日付パラメータの形式不正() {
    assertBadRequest(
        given()
            .queryParam("from", "2026/01/01")
            .queryParam("to", "2026-01-31")
            .queryParam("granularity", "month")
            .when()
            .get("/v1/balance-series")
            .then(),
        "fromはyyyy-MM-dd形式の日付を指定してください");
  }

  @Test
  void 日付パラメータに存在しない日付() {
    assertBadRequest(
        given()
            .queryParam("from", "2026-01-01")
            .queryParam("to", "2026-02-30")
            .queryParam("granularity", "month")
            .when()
            .get("/v1/balance-series")
            .then(),
        "toはyyyy-MM-dd形式の日付を指定してください");
  }

  @Test
  void granularityの不正値() {
    assertBadRequest(
        given()
            .queryParam("from", "2026-01-01")
            .queryParam("to", "2026-01-31")
            .queryParam("granularity", "fortnight")
            .when()
            .get("/v1/balance-series")
            .then(),
        "granularityはday, week, month, quarter, yearのいずれかを指定してください");
  }

  @Test
  void 空文字の任意パラメータは未指定として扱う() {
    given()
        .queryParam("year", 2026)
        .queryParam("month", "")
        .when()
        .get("/v1/balance-statistics")
        .then()
        .statusCode(200)
        .body("period.from", equalTo("2026-01-01"));
  }

  @Test
  void 存在しないパスは引き続き404() {
    given().when().get("/v1/not-exists").then().statusCode(404);
  }

  private static void assertBadRequest(ValidatableResponse response, String detail) {
    response
        .statusCode(400)
        .contentType("application/problem+json")
        .body("status", equalTo(400))
        .body("detail", equalTo(detail));
  }
}
