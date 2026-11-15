package dev.higuchi.homeexpense.core.model;

import java.util.Set;

public sealed interface ExpectedVersion {

  static ExpectedVersion of(long version) {
    return new OneOf(Set.of(version));
  }

  static ExpectedVersion oneOf(Set<Long> versions) {
    return new OneOf(versions);
  }

  static ExpectedVersion unconditional() {
    return Unconditional.INSTANCE;
  }

  boolean matches(long currentVersion);

  record OneOf(Set<Long> versions) implements ExpectedVersion {
    public OneOf {
      versions = Set.copyOf(versions);
    }

    @Override
    public boolean matches(long currentVersion) {
      return versions.contains(currentVersion);
    }
  }

  enum Unconditional implements ExpectedVersion {
    INSTANCE;

    @Override
    public boolean matches(long currentVersion) {
      return true;
    }
  }
}
