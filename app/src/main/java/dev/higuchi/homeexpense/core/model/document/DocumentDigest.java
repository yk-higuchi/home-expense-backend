package dev.higuchi.homeexpense.core.model.document;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public record DocumentDigest(String value) {

  private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9_-]{42}[AEIMQUYcgkosw048]");
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  public DocumentDigest {
    if (value == null || !FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("ドキュメントのダイジェストの形式が不正です: " + value);
    }
  }

  public static DocumentDigest of(Map<String, ?> document) {
    return ofCanonicalJson(JsonCanonicalizer.canonicalize(document));
  }

  static DocumentDigest ofCanonicalJson(String canonicalJson) {
    try {
      byte[] hash =
          MessageDigest.getInstance("SHA-256")
              .digest(canonicalJson.getBytes(StandardCharsets.UTF_8));
      return fromBytes(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  public static DocumentDigest fromBytes(byte[] bytes) {
    if (bytes == null || bytes.length != 32) {
      throw new IllegalArgumentException("SHA-256 のダイジェストは 32 バイトです");
    }
    return new DocumentDigest(ENCODER.encodeToString(bytes));
  }

  public static Optional<DocumentDigest> parse(String value) {
    return value != null && FORMAT.matcher(value).matches()
        ? Optional.of(new DocumentDigest(value))
        : Optional.empty();
  }

  public byte[] toBytes() {
    return DECODER.decode(value);
  }
}
