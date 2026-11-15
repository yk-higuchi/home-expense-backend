package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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

  private static String etagOf(Map<String, Object> document) {
    return "\"" + DocumentDigest.of(document).value() + "\"";
  }

  @Test
  void 経費属性の一覧は分類を指定するとページ送りなしで全件を並び順で返し並びのETagを付ける() {
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
    assertEquals(etagOf(Map.of("category", "固定費", "ids", ids)), response.header("ETag"));
  }

  @Test
  void 分類を指定しない経費属性の一覧は固定費の後に変動費を返しETagは付けない() {
    String prefix = "mixed-" + UUID.randomUUID();
    createExpenseAttribute(prefix + "-v", "変動費");
    createExpenseAttribute(prefix + "-f", "固定費");

    Response response = given().get("/v1/expense-attributes");
    response.then().statusCode(200).header("ETag", nullValue()).header("Link", nullValue());
    List<String> categories = response.path("expense_attributes.category");

    int lastFixed = categories.lastIndexOf("固定費");
    int firstVariable = categories.indexOf("変動費");
    assertTrue(lastFixed < firstVariable, "固定費がすべて変動費より前に並ぶ");
  }

  @Test
  void 収入属性の一覧はページ送りなしで全件を並び順で返し並びのETagを付ける() {
    String prefix = "income-list-" + UUID.randomUUID();
    List<String> created = new ArrayList<>();
    for (int i = 0; i < 22; i++) {
      created.add(createIncomeAttribute(prefix + "-" + i));
    }

    Response response = given().get("/v1/income-attributes");
    response.then().statusCode(200).header("Link", nullValue());
    List<String> ids = response.path("income_attributes.id");

    assertEquals(created, ids.subList(ids.size() - created.size(), ids.size()));
    assertEquals(etagOf(Map.of("ids", ids)), response.header("ETag"));
  }

  @Test
  void 属性を登録すると並びのETagが変わる() {
    String before = given().get("/v1/income-attributes").header("ETag");
    createIncomeAttribute("etag-" + UUID.randomUUID());
    assertNotEquals(before, given().get("/v1/income-attributes").header("ETag"));
  }
}
