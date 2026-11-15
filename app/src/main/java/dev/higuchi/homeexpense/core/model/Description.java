package dev.higuchi.homeexpense.core.model;

import java.util.Objects;

public record Description(String value) {

  public static final int MAX_LENGTH = 512;

  public Description {
    Objects.requireNonNull(value, "説明は必須です");
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("説明は" + MAX_LENGTH + "文字以内でなければなりません");
    }
  }
}
