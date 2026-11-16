package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AttributeListApiTest {

  private static String idOf(Response response) {
    String location = response.then().statusCode(201).extract().header("Location");
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private static String createExpenseAttribute(String name, String category) {
    return idOf(
        given()
            .contentType("application/json")
            .body(Map.of("name", name, "category", category))
            .post("/v1/expense-attributes"));
  }

  private static String createIncomeAttribute(String name) {
    return idOf(
        given()
            .contentType("application/json")
            .body(Map.of("name", name))
            .post("/v1/income-attributes"));
  }

  private static String etagOfBody(Response response) {
    try {
      Map<String, Object> body =
          new ObjectMapper().readValue(response.asString(), new TypeReference<>() {});
      return "\"" + DocumentDigest.of(body).value() + "\"";
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  void 支出属性の一覧は分類を指定するとページ送りなしで全件を並び順で返し一覧のETagを付ける() {
    String prefix = "list-" + UUID.randomUUID();
    List<String> created = new ArrayList<>();
    for (int i = 0; i < 25; i++) {
      created.add(createExpenseAttribute(prefix + "-" + i, "固定費"));
    }

    Response response = given().queryParam("category", "固定費").get("/v1/expense-attributes");
    response.then().statusCode(200).header("Link", nullValue());
    List<String> ids = response.path("expense_attributes.id");
    List<String> categories = response.path("expense_attributes.category");

    assertTrue(categories.stream().allMatch("固定費"::equals));
    assertEquals(created, ids.stream().filter(created::contains).toList());
    assertEquals(ids.subList(ids.size() - created.size(), ids.size()), created, "登録した属性は末尾に入る");
    assertEquals(etagOfBody(response), response.header("ETag"), "ETag は一覧の本文のダイジェスト");
  }

  @Test
  void 分類を指定しない支出属性の一覧は固定費の後に変動費を返す() {
    String prefix = "mixed-" + UUID.randomUUID();
    createExpenseAttribute(prefix + "-v", "変動費");
    createExpenseAttribute(prefix + "-f", "固定費");

    Response response = given().get("/v1/expense-attributes");
    response.then().statusCode(200).header("Link", nullValue());
    assertEquals(etagOfBody(response), response.header("ETag"));
    List<String> categories = response.path("expense_attributes.category");

    int lastFixed = categories.lastIndexOf("固定費");
    int firstVariable = categories.indexOf("変動費");
    assertTrue(lastFixed < firstVariable, "固定費がすべて変動費より前に並ぶ");
  }

  @Test
  void 収入属性の一覧はページ送りなしで全件を並び順で返し一覧のETagを付ける() {
    String prefix = "income-list-" + UUID.randomUUID();
    List<String> created = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      created.add(createIncomeAttribute(prefix + "-" + i));
    }

    Response response = given().get("/v1/income-attributes");
    response.then().statusCode(200).header("Link", nullValue());
    List<String> ids = response.path("income_attributes.id");

    assertEquals(created, ids.subList(ids.size() - created.size(), ids.size()));
    assertEquals(etagOfBody(response), response.header("ETag"), "ETag は一覧の本文のダイジェスト");
  }

  @Test
  void 属性の名前を変えると一覧のETagも変わる() {
    String id = createIncomeAttribute("rename-" + UUID.randomUUID());
    String before = given().get("/v1/income-attributes").header("ETag");
    given()
        .contentType("application/json")
        .header("If-Match", given().get("/v1/income-attributes/{id}", id).header("ETag"))
        .body(Map.of("name", "renamed-" + UUID.randomUUID()))
        .put("/v1/income-attributes/{id}", id)
        .then()
        .statusCode(204);
    assertNotEquals(before, given().get("/v1/income-attributes").header("ETag"));
  }

  @Test
  void 属性を登録すると一覧のETagが変わる() {
    String before = given().get("/v1/income-attributes").header("ETag");
    createIncomeAttribute("etag-" + UUID.randomUUID());
    assertNotEquals(before, given().get("/v1/income-attributes").header("ETag"));
  }
}
