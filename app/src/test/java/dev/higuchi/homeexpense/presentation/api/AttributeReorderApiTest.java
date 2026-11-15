package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AttributeReorderApiTest {

  private static String create(String path, Map<String, String> body) {
    String location =
        given()
            .contentType("application/json")
            .body(body)
            .post(path)
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private static Response fixedList() {
    return given().queryParam("category", "固定費").get("/v1/expense-attributes");
  }

  private static Response putOrder(String path, Object body, String ifMatch) {
    var request = given().contentType("application/json").body(body);
    if (ifMatch != null) {
      request.header("If-Match", ifMatch);
    }
    return request.put(path);
  }

  @Test
  void 経費属性の並びを置き換えると新しい並びのETagが返り一覧に反映される() {
    create("/v1/expense-attributes", Map.of("name", "re-" + UUID.randomUUID(), "category", "固定費"));
    create("/v1/expense-attributes", Map.of("name", "re-" + UUID.randomUUID(), "category", "固定費"));
    Response list = fixedList();
    List<String> ids = new ArrayList<>(list.path("expense_attributes.id"));
    Collections.reverse(ids);
    String attributeEtag =
        given().get("/v1/expense-attributes/{id}", ids.getFirst()).header("ETag");

    Response put =
        putOrder(
            "/v1/expense-attributes/order",
            Map.of("category", "固定費", "ids", ids),
            list.header("ETag"));

    put.then().statusCode(204);
    Response after = fixedList();
    assertEquals(ids, after.path("expense_attributes.id"));
    assertEquals(after.header("ETag"), put.header("ETag"));
    assertEquals(
        attributeEtag,
        given().get("/v1/expense-attributes/{id}", ids.getFirst()).header("ETag"),
        "並べ替えても属性ごとの ETag は変わらない");
  }

  @Test
  void 古い並びのETagでは412() {
    create(
        "/v1/expense-attributes", Map.of("name", "stale-" + UUID.randomUUID(), "category", "固定費"));
    Response list = fixedList();
    String stale = list.header("ETag");
    List<String> ids = list.path("expense_attributes.id");
    create(
        "/v1/expense-attributes", Map.of("name", "stale-" + UUID.randomUUID(), "category", "固定費"));

    putOrder("/v1/expense-attributes/order", Map.of("category", "固定費", "ids", ids), stale)
        .then()
        .statusCode(412)
        .contentType("application/problem+json");
  }

  @Test
  void 今の属性と過不足があれば422() {
    create(
        "/v1/expense-attributes", Map.of("name", "lack-" + UUID.randomUUID(), "category", "固定費"));
    Response list = fixedList();
    List<String> ids = list.path("expense_attributes.id");

    putOrder(
            "/v1/expense-attributes/order",
            Map.of("category", "固定費", "ids", ids.subList(1, ids.size())),
            list.header("ETag"))
        .then()
        .statusCode(422)
        .contentType("application/problem+json")
        .body("detail", containsString("並び"));
  }

  @Test
  void IfMatchがなければ428() {
    putOrder("/v1/expense-attributes/order", Map.of("category", "固定費", "ids", List.of()), null)
        .then()
        .statusCode(428);
    putOrder("/v1/income-attributes/order", Map.of("ids", List.of()), null).then().statusCode(428);
  }

  @Test
  void 本文の形が不正なら400() {
    String id = UUID.randomUUID().toString();
    putOrder("/v1/expense-attributes/order", Map.of("category", "固定費", "ids", List.of(id, id)), "*")
        .then()
        .statusCode(400)
        .body("detail", containsString("同じID"));
    putOrder(
            "/v1/expense-attributes/order", Map.of("category", "固定費", "ids", List.of("order")), "*")
        .then()
        .statusCode(400);
    putOrder("/v1/expense-attributes/order", Map.of("ids", List.of()), "*").then().statusCode(400);
    putOrder("/v1/expense-attributes/order", Map.of("category", "その他", "ids", List.of()), "*")
        .then()
        .statusCode(400);
  }

  @Test
  void 収入属性の並びも置き換えられる() {
    create("/v1/income-attributes", Map.of("name", "inc-re-" + UUID.randomUUID()));
    create("/v1/income-attributes", Map.of("name", "inc-re-" + UUID.randomUUID()));
    Response list = given().get("/v1/income-attributes");
    List<String> ids = new ArrayList<>(list.path("income_attributes.id"));
    Collections.reverse(ids);

    putOrder("/v1/income-attributes/order", Map.of("ids", ids), list.header("ETag"))
        .then()
        .statusCode(204)
        .header("ETag", notNullValue());
    assertEquals(ids, given().get("/v1/income-attributes").path("income_attributes.id"));
  }

  @Test
  void orderの経路はIDの経路より優先される() {
    putOrder("/v1/expense-attributes/order", Map.of("name", "x"), "*")
        .then()
        .statusCode(400)
        .body("detail", not(containsString("IDの形式")));
  }
}
