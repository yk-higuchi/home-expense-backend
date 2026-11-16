package dev.higuchi.homeexpense.core.model.document;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DocumentDigestTest {

  @Test
  void JCSで正規化したドキュメントのSHA256をbase64urlで表す() {
    Map<String, Object> document = new LinkedHashMap<>();
    document.put("b", 1);
    document.put("a", "x");
    assertEquals(
        DocumentDigest.ofCanonicalJson("{\"a\":\"x\",\"b\":1}"), DocumentDigest.of(document));
    assertEquals(
        "ungWv48Bz-pBQUDeXa4iI7ADYaOWF3qctBD_YfIAFa0",
        DocumentDigest.ofCanonicalJson("abc").value());
  }

  @Test
  void キーの順番が違っても同じ値になる() {
    Map<String, Object> first = new LinkedHashMap<>();
    first.put("name", "食費");
    first.put("category", "変動費");
    Map<String, Object> second = new LinkedHashMap<>();
    second.put("category", "変動費");
    second.put("name", "食費");
    assertEquals(DocumentDigest.of(first), DocumentDigest.of(second));
  }

  @Test
  void バイト列と相互に変換できる() {
    DocumentDigest digest = DocumentDigest.ofCanonicalJson("abc");
    assertEquals(32, digest.toBytes().length);
    assertEquals(digest, DocumentDigest.fromBytes(digest.toBytes()));
  }

  @Test
  void 正しい形式の文字列だけを受け付ける() {
    String value = DocumentDigest.ofCanonicalJson("abc").value();
    assertEquals(value, DocumentDigest.parse(value).orElseThrow().value());
    assertTrue(DocumentDigest.parse("3").isEmpty());
    assertTrue(DocumentDigest.parse(value + "A").isEmpty());
    assertTrue(DocumentDigest.parse(value.replace('-', '+')).isEmpty());
    assertThrows(IllegalArgumentException.class, () -> new DocumentDigest("abc"));
  }
}
