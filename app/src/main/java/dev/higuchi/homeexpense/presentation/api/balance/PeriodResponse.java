package dev.higuchi.homeexpense.presentation.api.balance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.LocalDate;

@RegisterForReflection
public record PeriodResponse(
    @JsonProperty("from") LocalDate from, @JsonProperty("to") LocalDate to) {}
