package dev.yhiguchi.home_expense.domain.model;

import java.util.Set;

/**
 * 楽観ロックで更新を許可するバージョン条件（経費・収入で共通）。
 *
 * <p>HTTP の If-Match（RFC 9110 §13.1.1）に対応する。{@link OneOf} は列挙したいずれかのバージョンと一致すれば更新を許可し、 {@link
 * Unconditional} は現在の表現が存在すればバージョンを問わず許可する（If-Match: *）。
 */
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

  /** いずれかのバージョンと一致すれば許可。空の場合はどのバージョンとも一致しない */
  record OneOf(Set<Long> versions) implements ExpectedVersion {
    public OneOf {
      versions = Set.copyOf(versions);
    }

    @Override
    public boolean matches(long currentVersion) {
      return versions.contains(currentVersion);
    }
  }

  /** バージョンを問わず許可 */
  enum Unconditional implements ExpectedVersion {
    INSTANCE;

    @Override
    public boolean matches(long currentVersion) {
      return true;
    }
  }
}
