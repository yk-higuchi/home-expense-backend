package dev.higuchi.homeexpense.presentation.api;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class IfMatchParser {

  private static final Pattern ENTITY_TAG =
      Pattern.compile("(W/)?\"([\\x21\\x23-\\x7E\\x80-\\xFF]*)\"");

  private IfMatchParser() {}

  public static ExpectedDigest parse(String headerValue) {
    if (headerValue == null || headerValue.isBlank()) {
      throw new PreconditionRequiredException("If-Matchヘッダーは必須です");
    }
    String value = headerValue.strip();
    if (value.equals("*")) {
      return ExpectedDigest.unconditional();
    }
    Set<DocumentDigest> digests = new HashSet<>();
    boolean hasElement = false;
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
      if (!weak) {
        DocumentDigest.parse(opaque).ifPresent(digests::add);
      }
    }
    if (!hasElement) {
      throw new InvalidIfMatchHeaderException("If-Matchヘッダーの形式が不正です");
    }
    return ExpectedDigest.oneOf(digests);
  }
}
