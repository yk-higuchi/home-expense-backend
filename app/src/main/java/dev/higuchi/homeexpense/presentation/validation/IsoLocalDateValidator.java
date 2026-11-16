package dev.higuchi.homeexpense.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class IsoLocalDateValidator implements ConstraintValidator<IsoLocalDate, String> {

  private static final Pattern FORMAT = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    if (!FORMAT.matcher(value).matches()) {
      return false;
    }
    try {
      LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}
