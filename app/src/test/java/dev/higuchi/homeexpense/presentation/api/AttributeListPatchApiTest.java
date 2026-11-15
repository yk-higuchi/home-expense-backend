package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AttributeListPatchApiTest {

  private static final String JSON_PATCH = "application/json-patch+json";

  private static String createExpenseAttribute(String category) {
    String location =
        given()
            .contentType("application/json")
            .body(Map.of("name", "patch-" + UUID.randomUUID(), "category", category))
            .post("/v1/expense-attributes")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private static Response fixedList() {
    return given().queryParam("category", "固定費").get("/v1/expense-attributes");
  }

  private static Response patchFixed(Object body, String ifMatch) {
    var request = given().contentType(JSON_PATCH).queryParam("category", "固定費").body(body);
    if (ifMatch != null) {
      request.header("If-Match", ifMatch);
    }
    return request.patch("/v1/expense-attributes");
  }

  private static Map<String, Object> move(String from, String path) {
    return Map.of("op", "move", "from", from, "path", path);
  }

  @Test
  void moveで並べ替えると新しい一覧のETagが返り一覧に反映される() {
    createExpenseAttribute("固定費");
    String last = createExpenseAttribute("固定費");
    Response list = fixedList();
    List<String> ids = list.path("expense_attributes.id");
    int lastIndex = ids.indexOf(last);
    String attributeEtag = given().get("/v1/expense-attributes/{id}", last).header("ETag");

    Response patched =
        patchFixed(
            List.of(move("/expense_attributes/" + lastIndex, "/expense_attributes/0")),
            list.header("ETag"));

    patched.then().statusCode(204);
    Response after = fixedList();
    List<String> afterIds = after.path("expense_attributes.id");
    assertEquals(last, afterIds.getFirst());
    assertEquals(ids.size(), afterIds.size());
    assertEquals(after.header("ETag"), patched.header("ETag"));
    assertEquals(
        attributeEtag,
        given().get("/v1/expense-attributes/{id}", last).header("ETag"),
        "並べ替えても属性ごとの ETag は変わらない");
  }

  @Test
  void 末尾への移動はハイフンで指定できる() {
    String first = createExpenseAttribute("固定費");
    createExpenseAttribute("固定費");
    Response list = fixedList();
    int index = list.<List<String>>path("expense_attributes.id").indexOf(first);

    patchFixed(
            List.of(move("/expense_attributes/" + index, "/expense_attributes/-")),
            list.header("ETag"))
        .then()
        .statusCode(204);
    assertEquals(first, fixedList().<List<String>>path("expense_attributes.id").getLast());
  }

  @Test
  void ほかの人が名前を変えたあとの古いETagでは412() {
    String id = createExpenseAttribute("固定費");
    createExpenseAttribute("固定費");
    String stale = fixedList().header("ETag");
    given()
        .contentType("application/json")
        .header("If-Match", given().get("/v1/expense-attributes/{id}", id).header("ETag"))
        .body(Map.of("name", "renamed-" + UUID.randomUUID()))
        .put("/v1/expense-attributes/{id}", id)
        .then()
        .statusCode(204);

    patchFixed(List.of(move("/expense_attributes/0", "/expense_attributes/1")), stale)
        .then()
        .statusCode(412)
        .contentType("application/problem+json");
  }

  @Test
  void 範囲外の位置は422() {
    createExpenseAttribute("固定費");
    Response list = fixedList();
    int size = list.<List<String>>path("expense_attributes.id").size();

    patchFixed(
            List.of(move("/expense_attributes/" + size, "/expense_attributes/0")),
            list.header("ETag"))
        .then()
        .statusCode(422)
        .body("detail", containsString("範囲外"));
  }

  @Test
  void move以外の操作や対象外のパスは422() {
    createExpenseAttribute("固定費");
    String etag = fixedList().header("ETag");
    patchFixed(List.of(Map.of("op", "remove", "path", "/expense_attributes/0")), etag)
        .then()
        .statusCode(422)
        .body("detail", containsString("move"));
    patchFixed(
            List.of(Map.of("op", "replace", "path", "/expense_attributes/0/name", "value", "x")),
            etag)
        .then()
        .statusCode(422);
    patchFixed(List.of(move("/expense_attributes/0/name", "/expense_attributes/1")), etag)
        .then()
        .statusCode(422);
    patchFixed(List.of(move("/income_attributes/0", "/income_attributes/1")), etag)
        .then()
        .statusCode(422);
  }

  @Test
  void 区分の指定がなければ400() {
    given()
        .contentType(JSON_PATCH)
        .header("If-Match", "*")
        .body(List.of(move("/expense_attributes/0", "/expense_attributes/1")))
        .patch("/v1/expense-attributes")
        .then()
        .statusCode(400)
        .body("detail", containsString("category"));
  }

  @Test
  void IfMatchがなければ428() {
    patchFixed(List.of(), null).then().statusCode(428);
    given()
        .contentType(JSON_PATCH)
        .body(List.of())
        .patch("/v1/income-attributes")
        .then()
        .statusCode(428);
  }

  @Test
  void JSONPatch以外の形式は415で_配列でない本文は400() {
    given()
        .contentType("application/json")
        .queryParam("category", "固定費")
        .header("If-Match", "*")
        .body(List.of())
        .patch("/v1/expense-attributes")
        .then()
        .statusCode(415);
    patchFixed(Map.of("op", "move"), "*").then().statusCode(400);
  }

  @Test
  void 空のパッチは何も変えずに今のETagを返す() {
    String etag = fixedList().header("ETag");
    patchFixed(List.of(), etag).then().statusCode(204).header("ETag", equalTo(etag));
  }

  @Test
  void 収入属性の一覧もmoveで並べ替えられる() {
    String location =
        given()
            .contentType("application/json")
            .body(Map.of("name", "inc-patch-" + UUID.randomUUID()))
            .post("/v1/income-attributes")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    String id = location.substring(location.lastIndexOf('/') + 1);
    Response list = given().get("/v1/income-attributes");
    int index = list.<List<String>>path("income_attributes.id").indexOf(id);

    given()
        .contentType(JSON_PATCH)
        .header("If-Match", list.header("ETag"))
        .body(List.of(move("/income_attributes/" + index, "/income_attributes/0")))
        .patch("/v1/income-attributes")
        .then()
        .statusCode(204)
        .header("ETag", notNullValue());
    assertEquals(
        id,
        given().get("/v1/income-attributes").<List<String>>path("income_attributes.id").getFirst());
  }
}
