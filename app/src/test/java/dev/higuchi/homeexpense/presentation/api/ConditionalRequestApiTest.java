package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class ConditionalRequestApiTest {

  private String path;

  @BeforeEach
  void setUp() {
    String location =
        given()
            .contentType("application/json")
            .body("{\"name\":\"cond-" + UUID.randomUUID() + "\"}")
            .post("/v1/income-attributes")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    path = location.replaceFirst("^https?://[^/]+", "");
  }

  private String body() {
    return "{\"name\":\"cond-" + UUID.randomUUID() + "\"}";
  }

  @Test
  void GETは強いETagを返す() {
    given().get(path).then().statusCode(200).header("ETag", equalTo("\"1\""));
  }

  @Test
  void IfMatchなしは428() {
    given()
        .contentType("application/json")
        .body(body())
        .put(path)
        .then()
        .statusCode(428)
        .contentType("application/problem+json")
        .body("title", equalTo("Precondition Required"))
        .body("detail", equalTo("If-Matchヘッダーは必須です"));
  }

  @Test
  void 一致するETagなら更新でき新しいETagに変わる() {
    given()
        .contentType("application/json")
        .header("If-Match", "\"1\"")
        .body(body())
        .put(path)
        .then()
        .statusCode(204);
    given().get(path).then().header("ETag", equalTo("\"2\""));
  }

  @Test
  void 一致しないETagは412() {
    given()
        .contentType("application/json")
        .header("If-Match", "\"9\"")
        .body(body())
        .put(path)
        .then()
        .statusCode(412)
        .contentType("application/problem+json");
  }

  @Test
  void 弱いETagは強い比較で一致しないため412() {
    given()
        .contentType("application/json")
        .header("If-Match", "W/\"1\"")
        .body(body())
        .put(path)
        .then()
        .statusCode(412);
  }

  @Test
  void ETagのリストはいずれかが一致すれば更新できる() {
    given()
        .contentType("application/json")
        .header("If-Match", "\"9\", \"1\"")
        .body(body())
        .put(path)
        .then()
        .statusCode(204);
  }

  @Test
  void アスタリスクは現在の表現があれば更新できる() {
    given()
        .contentType("application/json")
        .header("If-Match", "*")
        .body(body())
        .put(path)
        .then()
        .statusCode(204);
  }

  @Test
  void 構文不正なIfMatchは400() {
    given()
        .contentType("application/json")
        .header("If-Match", "1")
        .body(body())
        .put(path)
        .then()
        .statusCode(400)
        .contentType("application/problem+json")
        .body("detail", equalTo("If-Matchヘッダーの形式が不正です"));
  }

  @Test
  void 内容に変更がなくてもETagが一致しなければ412() {
    String name = given().get(path).then().extract().path("name");
    given()
        .contentType("application/json")
        .header("If-Match", "\"9\"")
        .body("{\"name\":\"" + name + "\"}")
        .put(path)
        .then()
        .statusCode(412);
  }

  @Test
  void DELETEもIfMatchなしは428() {
    given().delete(path).then().statusCode(428).contentType("application/problem+json");
    given().get(path).then().statusCode(200);
  }

  @Test
  void DELETEで一致しないETagは412で削除しない() {
    given().header("If-Match", "\"9\"").delete(path).then().statusCode(412);
    given().get(path).then().statusCode(200);
  }

  @Test
  void DELETEで一致するETagなら削除できる() {
    given().header("If-Match", "\"1\"").delete(path).then().statusCode(204);
    given().get(path).then().statusCode(404);
  }

  @Test
  void DELETEでアスタリスクならバージョンを問わず削除できる() {
    given().header("If-Match", "*").delete(path).then().statusCode(204);
    given().get(path).then().statusCode(404);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/expenses/",
        "/v1/expense-attributes/",
        "/v1/incomes/",
        "/v1/income-attributes/"
      })
  void すべてのDELETEでIfMatchが必須(String collection) {
    given().delete(collection + UUID.randomUUID()).then().statusCode(428);
  }

  @Test
  void 一覧の各要素のversionは個別取得のETagと一致する() {
    String id = path.substring(path.lastIndexOf('/') + 1);
    given()
        .contentType("application/json")
        .header("If-Match", "\"1\"")
        .body(body())
        .put(path)
        .then()
        .statusCode(204);
    given().get(path).then().header("ETag", equalTo("\"2\"")).body("version", equalTo(2));
    given()
        .queryParam("per_page", 100)
        .queryParam("page", 1)
        .get("/v1/income-attributes")
        .then()
        .statusCode(200)
        .body("income_attributes.find { it.id == '%s' }.version".formatted(id), equalTo(2));
  }
}
