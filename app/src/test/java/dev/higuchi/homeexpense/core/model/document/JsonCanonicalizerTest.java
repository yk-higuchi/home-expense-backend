package dev.higuchi.homeexpense.core.model.document;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonCanonicalizerTest {

  @Test
  void RFC8785の文字列のエスケープの例と一致する() {
    String input = "\u20ac$\u000F\nA'B\"\\\\\"/";
    assertEquals("\"€$\\u000f\\nA'B\\\"\\\\\\\\\\\"/\"", JsonCanonicalizer.canonicalize(input));
  }

  @Test
  void RFC8785のプロパティの並び替えの例と一致する() {
    Map<String, Object> input = new LinkedHashMap<>();
    input.put("\u20ac", "Euro Sign");
    input.put("\r", "Carriage Return");
    input.put("\ufb33", "Hebrew Letter Dalet With Dagesh");
    input.put("1", "One");
    input.put("\ud83d\ude00", "Emoji: Grinning Face");
    input.put("\u0080", "Control");
    input.put("\u00f6", "Latin Small Letter O With Diaeresis");
    assertEquals(
        "{\"\\r\":\"Carriage Return\",\"1\":\"One\",\"\u0080\":\"Control\","
            + "\"\u00f6\":\"Latin Small Letter O With Diaeresis\",\"\u20ac\":\"Euro Sign\","
            + "\"\ud83d\ude00\":\"Emoji: Grinning Face\","
            + "\"\ufb33\":\"Hebrew Letter Dalet With Dagesh\"}",
        JsonCanonicalizer.canonicalize(input));
  }

  @Test
  void 制御文字は短い形または小文字の16進でエスケープする() {
    String input = "\b\t\n\f\r\u0001\u001f\u007f\u2028日本<>&";
    assertEquals(
        "\"\\b\\t\\n\\f\\r\\u0001\\u001f\u007f\u2028日本<>&\"",
        JsonCanonicalizer.canonicalize(input));
  }

  @Test
  void 入れ子のオブジェクトと配列と真偽値とnullを出力する() {
    Map<String, Object> nested = new LinkedHashMap<>();
    nested.put("b", true);
    nested.put("a", null);
    Map<String, Object> input = new LinkedHashMap<>();
    input.put("list", List.of(1, "x", nested));
    input.put("flag", false);
    assertEquals(
        "{\"flag\":false,\"list\":[1,\"x\",{\"a\":null,\"b\":true}]}",
        JsonCanonicalizer.canonicalize(input));
  }

  @Test
  void 整数はそのまま十進で出力する() {
    assertEquals(
        "[0,-1,1000000000000,9007199254740991]",
        JsonCanonicalizer.canonicalize(List.of(0, -1, 1_000_000_000_000L, 9_007_199_254_740_991L)));
  }

  @Test
  void JSONの数値で誤差なく表せない整数は拒否する() {
    assertThrows(
        IllegalArgumentException.class,
        () -> JsonCanonicalizer.canonicalize(9_007_199_254_740_992L));
  }

  @Test
  void 整数以外の数値は拒否する() {
    assertThrows(IllegalArgumentException.class, () -> JsonCanonicalizer.canonicalize(1.5));
    assertThrows(
        IllegalArgumentException.class, () -> JsonCanonicalizer.canonicalize(new BigDecimal("1")));
  }

  @Test
  void 対になっていないサロゲートは拒否する() {
    assertThrows(IllegalArgumentException.class, () -> JsonCanonicalizer.canonicalize("\ud83d"));
    assertThrows(IllegalArgumentException.class, () -> JsonCanonicalizer.canonicalize("\ude00a"));
  }

  @Test
  void 文字列以外のキーは拒否する() {
    assertThrows(
        IllegalArgumentException.class, () -> JsonCanonicalizer.canonicalize(Map.of(1, "x")));
  }
}
