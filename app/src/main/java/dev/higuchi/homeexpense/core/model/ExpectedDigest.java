package dev.higuchi.homeexpense.core.model;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.Set;

public sealed interface ExpectedDigest {

  static ExpectedDigest of(DocumentDigest digest) {
    return new OneOf(Set.of(digest));
  }

  static ExpectedDigest oneOf(Set<DocumentDigest> digests) {
    return new OneOf(digests);
  }

  static ExpectedDigest unconditional() {
    return Unconditional.INSTANCE;
  }

  boolean matches(DocumentDigest current);

  record OneOf(Set<DocumentDigest> digests) implements ExpectedDigest {
    public OneOf {
      digests = Set.copyOf(digests);
    }

    @Override
    public boolean matches(DocumentDigest current) {
      return digests.contains(current);
    }
  }

  enum Unconditional implements ExpectedDigest {
    INSTANCE;

    @Override
    public boolean matches(DocumentDigest current) {
      return true;
    }
  }
}
