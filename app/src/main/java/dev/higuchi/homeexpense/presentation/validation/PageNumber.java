package dev.higuchi.homeexpense.presentation.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dev.higuchi.homeexpense.query.Pagination;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Min(value = Pagination.PAGE_MIN, message = "pageは{value}以上を指定してください")
@Max(value = Pagination.PAGE_MAX, message = "pageは{value}以下を指定してください")
@Constraint(validatedBy = {})
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
@Documented
public @interface PageNumber {
  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
