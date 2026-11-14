package dev.higuchi.homeexpense.presentation.api;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * If-Match ヘッダー（RFC 9110 §13.1.1）を {@link ExpectedVersion} に変換する。
 *
 * <ul>
 *   <li>未指定 → {@link PreconditionRequiredException}（428, RFC 6585）
 *   <li>{@code *} → バージョンを問わず更新を許可
 *   <li>entity-tag のリスト → 強い比較で一致し得るバージョン（数値の強い ETag）の集合。弱い ETag や数値以外の ETag
 *       はどの表現とも一致しないため無視する（結果が空なら 412）
 *   <li>構文不正 → {@link InvalidIfMatchHeaderException}（400）
 * </ul>
 */
public final class IfMatchParser {

  /** entity-tag = [ "W/" ] DQUOTE *etagc DQUOTE, etagc = %x21 / %x23-7E / obs-text */
  private static final Pattern ENTITY_TAG =
      Pattern.compile("(W/)?\"([\\x21\\x23-\\x7E\\x80-\\xFF]*)\"");

  private static final Pattern VERSION = Pattern.compile("\\d{1,18}");

  private IfMatchParser() {}

  public static ExpectedVersion parse(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      throw new PreconditionRequiredException("If-Matchヘッダーは必須です");
    }
    String value = headerValue.strip();
    if (value.equals("*")) {
      return ExpectedVersion.unconditional();
    }
    Set<Long> versions = new HashSet<>();
    boolean hasElement = false;
    // #entity-tag: カンマ区切り。空要素は無視する（RFC 9110 §5.6.1.2）
    for (String element : value.split(",", -1)) {
      String tag = element.strip();
      if (tag.isEmpty()) {
        continue;
      }
      var matcher = ENTITY_TAG.matcher(tag);
      if (!matcher.matches()) {
        throw new InvalidIfMatchHeaderException("If-Matchヘッダーの形式が不正です");
      }
      hasElement = true;
      boolean weak = matcher.group(1) != null;
      String opaque = matcher.group(2);
      if (!weak && VERSION.matcher(opaque).matches()) {
        versions.add(Long.parseLong(opaque));
      }
    }
    if (!hasElement) {
      throw new InvalidIfMatchHeaderException("If-Matchヘッダーの形式が不正です");
    }
    return ExpectedVersion.oneOf(versions);
  }
}
