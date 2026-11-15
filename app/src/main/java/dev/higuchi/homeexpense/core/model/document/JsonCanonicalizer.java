package dev.higuchi.homeexpense.core.model.document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonCanonicalizer {

  private static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

  private JsonCanonicalizer() {}

  public static String canonicalize(Object value) {
    StringBuilder sb = new StringBuilder();
    write(value, sb);
    return sb.toString();
  }

  private static void write(Object value, StringBuilder sb) {
    switch (value) {
      case null -> sb.append("null");
      case String s -> writeString(s, sb);
      case Boolean b -> sb.append(b ? "true" : "false");
      case Integer i -> sb.append(i);
      case Long l -> writeLong(l, sb);
      case Map<?, ?> map -> writeObject(map, sb);
      case List<?> list -> writeArray(list, sb);
      default -> throw new IllegalArgumentException("JCS で扱えない値です: " + value.getClass().getName());
    }
  }

  private static void writeLong(long value, StringBuilder sb) {
    if (value > MAX_SAFE_INTEGER || value < -MAX_SAFE_INTEGER) {
      throw new IllegalArgumentException("JSON の数値で誤差なく表せない整数です: " + value);
    }
    sb.append(value);
  }

  private static void writeObject(Map<?, ?> map, StringBuilder sb) {
    List<String> keys = new ArrayList<>(map.size());
    for (Object key : map.keySet()) {
      if (!(key instanceof String s)) {
        throw new IllegalArgumentException("オブジェクトのキーは文字列だけです: " + key);
      }
      keys.add(s);
    }
    keys.sort(String::compareTo);
    sb.append('{');
    for (int i = 0; i < keys.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      writeString(keys.get(i), sb);
      sb.append(':');
      write(map.get(keys.get(i)), sb);
    }
    sb.append('}');
  }

  private static void writeArray(List<?> list, StringBuilder sb) {
    sb.append('[');
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        sb.append(',');
      }
      write(list.get(i), sb);
    }
    sb.append(']');
  }

  private static void writeString(String value, StringBuilder sb) {
    sb.append('"');
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (Character.isHighSurrogate(ch)) {
        if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(i + 1))) {
          throw new IllegalArgumentException("対になっていないサロゲートを含みます");
        }
        sb.append(ch).append(value.charAt(++i));
        continue;
      }
      if (Character.isLowSurrogate(ch)) {
        throw new IllegalArgumentException("対になっていないサロゲートを含みます");
      }
      switch (ch) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\b' -> sb.append("\\b");
        case '\t' -> sb.append("\\t");
        case '\n' -> sb.append("\\n");
        case '\f' -> sb.append("\\f");
        case '\r' -> sb.append("\\r");
        default -> {
          if (ch < 0x20) {
            sb.append(String.format("\\u%04x", (int) ch));
          } else {
            sb.append(ch);
          }
        }
      }
    }
    sb.append('"');
  }
}
