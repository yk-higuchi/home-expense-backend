package dev.higuchi.homeexpense.testing;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.Map;

public final class TestDigests {

  public static final DocumentDigest STALE = DocumentDigest.of(Map.of("stale", true));

  private TestDigests() {}
}
