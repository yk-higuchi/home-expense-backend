package dev.yhiguchi.home_expense.presentation.api.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

/** 期間（from / to ともに含む） */
public record PeriodResponse(
    @JsonProperty("from") LocalDate from, @JsonProperty("to") LocalDate to) {}
