package dev.yhiguchi.home_expense.presentation.problem;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/** フレームワークが返す HTTP エラーも RFC 9457 Problem Details で返る */
@QuarkusTest
class FrameworkErrorProblemDetailApiTest {

  @Test
  void 不正なJSON本文は400のProblemDetails() {
    given()
        .contentType("application/json")
        .body("{bad")
        .post("/v1/income-attributes")
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("type", equalTo("about:blank"))
        .body("title", equalTo("Bad Request"))
        .body("status", equalTo(400))
        .body("instance", nullValue());
  }

  @Test
  void 未定義のパスは404のProblemDetails() {
    given()
        .get("/v1/not-exists")
        .then()
        .statusCode(404)
        .contentType("application/problem+json")
        .body("title", equalTo("Not Found"));
  }

  @Test
  void 許可されないメソッドは405のProblemDetailsでAllowヘッダーを返す() {
    given()
        .delete("/v1/incomes")
        .then()
        .statusCode(405)
        .contentType("application/problem+json")
        .header("Allow", notNullValue());
  }

  @Test
  void 未対応のメディアタイプは415のProblemDetails() {
    given()
        .contentType("text/plain")
        .body("x")
        .post("/v1/income-attributes")
        .then()
        .statusCode(415)
        .contentType("application/problem+json");
  }
}
