package dev.higuchi.homeexpense.presentation.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AttributeLimitApiTest {

  private static final int LIMIT = 100;

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

  private static void deleteAll(String path, List<String> ids) {
    for (String id : ids) {
      given().header("If-Match", "*").delete(path + "/" + id).then().statusCode(204);
    }
  }

  @Test
  void 経費属性は分類ごとに100件を超えて登録すると409() {
    String path = "/v1/expense-attributes";
    int existing =
        given().queryParam("category", "変動費").get(path).path("expense_attributes.size()");
    List<String> created = new ArrayList<>();
    try {
      for (int i = existing; i < LIMIT; i++) {
        created.add(create(path, Map.of("name", "limit-" + UUID.randomUUID(), "category", "変動費")));
      }
      given()
          .contentType("application/json")
          .body(Map.of("name", "limit-over-" + UUID.randomUUID(), "category", "変動費"))
          .post(path)
          .then()
          .statusCode(409)
          .contentType("application/problem+json")
          .body("detail", equalTo("変動費の経費属性は100件まで登録できます"));
      created.add(
          create(path, Map.of("name", "limit-other-" + UUID.randomUUID(), "category", "固定費")));
    } finally {
      deleteAll(path, created);
    }
  }

  @Test
  void 収入属性は100件を超えて登録すると409() {
    String path = "/v1/income-attributes";
    int existing = given().get(path).path("income_attributes.size()");
    List<String> created = new ArrayList<>();
    try {
      for (int i = existing; i < LIMIT; i++) {
        created.add(create(path, Map.of("name", "limit-" + UUID.randomUUID())));
      }
      given()
          .contentType("application/json")
          .body(Map.of("name", "limit-over-" + UUID.randomUUID()))
          .post(path)
          .then()
          .statusCode(409)
          .body("detail", equalTo("収入属性は100件まで登録できます"));
    } finally {
      deleteAll(path, created);
    }
  }
}
