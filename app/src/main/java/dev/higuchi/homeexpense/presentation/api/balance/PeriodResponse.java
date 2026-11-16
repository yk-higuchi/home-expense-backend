package dev.higuchi.homeexpense.presentation.api.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record PeriodResponse(
    @JsonProperty("from") LocalDate from, @JsonProperty("to") LocalDate to) {}
