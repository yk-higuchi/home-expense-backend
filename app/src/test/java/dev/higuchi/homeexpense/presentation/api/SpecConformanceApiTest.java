package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SpecConformanceApiTest {

  private static String expenseAttributeId() {
    String location =
        given()
            .contentType("application/json")
            .body("{\"name\":\"spec-" + UUID.randomUUID() + "\",\"category\":\"変動費\"}")
            .post("/v1/expense-attributes")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private static void assertProblem(ValidatableResponse response, int status, String detail) {
    response
        .statusCode(status)
        .contentType("application/problem+json")
        .body("status", equalTo(status))
        .body("detail", containsString(detail));
  }

  @Test
  void 存在しない日付は500ではなく400() {
    assertProblem(
        given()
            .contentType("application/json")
            .body(
                "{\"description\":\"x\",\"amount\":1,\"payment_date\":\"2026-02-30\",\"attribute_id\":\""
                    + expenseAttributeId()
                    + "\"}")
            .post("/v1/expenses")
            .then(),
        400,
        "payment_dateの形式に誤りがあります");
  }

  @Test
  void 経費の参照先属性が存在しなければ422() {
    assertProblem(
        given()
            .contentType("application/json")
            .body(
                "{\"description\":\"x\",\"amount\":1,\"payment_date\":\"2026-02-01\",\"attribute_id\":\""
                    + UUID.randomUUID()
                    + "\"}")
            .post("/v1/expenses")
            .then(),
        422,
        "指定された経費属性が存在しません");
  }

  @Test
  void 収入の参照先属性が存在しなければ422() {
    assertProblem(
        given()
            .contentType("application/json")
            .body(
                "{\"description\":\"x\",\"amount\":1,\"receive_date\":\"2026-02-01\",\"attribute_id\":\""
                    + UUID.randomUUID()
                    + "\"}")
            .post("/v1/incomes")
            .then(),
        422,
        "指定された収入属性が存在しません");
  }

  @Test
  void attribute_idがUUID形式でなければ400() {
    assertProblem(given().queryParam("attribute_id", "xyz").get("/v1/expenses").then(), 400, "形式");
  }

  @Test
  void yearが上限2200を超えたら400() {
    assertProblem(given().queryParam("year", 2201).get("/v1/expenses").then(), 400, "yearは1〜2200");
    assertProblem(given().queryParam("year", 2201).get("/v1/incomes").then(), 400, "yearは1〜2200");
  }

  @Test
  void pageが上限を超えたら400() {
    assertProblem(
        given().queryParam("page", 1_000_001).get("/v1/incomes").then(), 400, "pageは1000000以下");
  }

  @Test
  void yearなしのmonthは400() {
    assertProblem(
        given().queryParam("month", 4).get("/v1/expenses").then(),
        400,
        "monthを指定する場合はyearも指定してください");
  }

  @Test
  void リクエストに定義されていないプロパティは400() {
    given()
        .contentType("application/json")
        .body("{\"name\":\"spec-" + UUID.randomUUID() + "\",\"category\":\"変動費\",\"extra\":1}")
        .post("/v1/expense-attributes")
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("status", equalTo(400))
        .body("detail", equalTo("定義されていないプロパティが含まれています: extra"));
  }

  @Test
  void 型が合わない値は400() {
    given()
        .contentType("application/json")
        .body(
            "{\"description\":\"x\",\"amount\":\"abc\",\"payment_date\":\"2026-02-01\",\"attribute_id\":\""
                + expenseAttributeId()
                + "\"}")
        .post("/v1/expenses")
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("status", equalTo(400))
        .body("detail", equalTo("amountの値の形式が不正です"));
  }
}
