package dev.higuchi.homeexpense.presentation.validation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** yyyy-MM-dd 形式で、かつ暦上存在する日付（例: 2026-02-30 は不可）。null は対象外 */
@Constraint(validatedBy = {IsoLocalDateValidator.class})
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
@Documented
public @interface IsoLocalDate {
  String message() default "日付の形式に誤りがあります";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
