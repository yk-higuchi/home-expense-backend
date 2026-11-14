package dev.yhiguchi.home_expense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/** Link ヘッダー（RFC 8288） */
@QuarkusTest
class LinkHeaderApiTest {

  @Test
  void 関係型はIANA登録済みのself_first_lastを使う() {
    given()
        .queryParam("year", 1999)
        .get("/v1/incomes")
        .then()
        .statusCode(200)
        .header("Link", containsString("rel=\"self\""))
        .header("Link", containsString("rel=\"first\""))
        .header("Link", containsString("rel=\"last\""))
        .header("Link", not(containsString("rel=\"current\"")))
        .header("Link", not(containsString("rel=\"previous\"")));
  }

  @Test
  void リンクは絞り込み条件のクエリパラメータを保持する() {
    given()
        .queryParam("year", 1999)
        .queryParam("per_page", 5)
        .get("/v1/incomes")
        .then()
        .header("Link", containsString("year=1999"))
        .header("Link", not(containsString("page=0")));
  }

  @Test
  void 件数0なら最終ページは1で次ページはない() {
    given()
        .queryParam("year", 1999)
        .get("/v1/incomes")
        .then()
        .header("Link", containsString("page=1&per_page=20>; rel=\"last\""))
        .header("Link", not(containsString("rel=\"next\"")));
  }

  @Test
  void 最終ページを超えたページでは次ページはなく前ページは最終ページを指す() {
    given()
        .queryParam("year", 1999)
        .queryParam("page", 5)
        .get("/v1/incomes")
        .then()
        .header("Link", not(containsString("rel=\"next\"")))
        .header("Link", containsString("page=1&per_page=20>; rel=\"prev\""));
  }
}
