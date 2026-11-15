package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import io.quarkus.test.junit.QuarkusTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
class ConditionalRequestApiTest {

  private String path;
  private String name;

  @BeforeEach
  void setUp() {
    name = "cond-" + UUID.randomUUID();
    String location =
        given()
            .contentType("application/json")
            .body("{\"name\":\"" + name + "\"}")
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

  private String etag() {
    return given().get(path).then().statusCode(200).extract().header("ETag");
  }

  private static String etagOf(String name) {
    return "\"" + DocumentDigest.of(Map.of("name", name)).value() + "\"";
  }

  @Test
  void GETはドキュメントのダイジェストを強いETagとして返す() {
    given().get(path).then().statusCode(200).header("ETag", equalTo(etagOf(name)));
  }

  @Test
  void 本文には版の情報を含めない() {
    given().get(path).then().statusCode(200).body("$", not(hasKey("version")));
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
    String newName = "cond-" + UUID.randomUUID();
    given()
        .contentType("application/json")
        .header("If-Match", etag())
        .body("{\"name\":\"" + newName + "\"}")
        .put(path)
        .then()
        .statusCode(204);
    given().get(path).then().header("ETag", equalTo(etagOf(newName)));
  }

  @Test
  void 内容が変わらない更新ではETagも変わらない() {
    String before = etag();
    given()
        .contentType("application/json")
        .header("If-Match", before)
        .body("{\"name\":\"" + name + "\"}")
        .put(path)
        .then()
        .statusCode(204);
    given().get(path).then().header("ETag", equalTo(before));
  }

  @Test
  void 古いETagは412() {
    String stale = etag();
    given()
        .contentType("application/json")
        .header("If-Match", stale)
        .body(body())
        .put(path)
        .then()
        .statusCode(204);
    given()
        .contentType("application/json")
        .header("If-Match", stale)
        .body(body())
        .put(path)
        .then()
        .statusCode(412);
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
        .header("If-Match", "W/" + etag())
        .body(body())
        .put(path)
        .then()
        .statusCode(412);
  }

  @Test
  void ETagのリストはいずれかが一致すれば更新できる() {
    given()
        .contentType("application/json")
        .header("If-Match", "\"9\", " + etag())
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
    given().header("If-Match", etag()).delete(path).then().statusCode(204);
    given().get(path).then().statusCode(404);
  }

  @Test
  void DELETEでアスタリスクなら条件なしで削除できる() {
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
}
