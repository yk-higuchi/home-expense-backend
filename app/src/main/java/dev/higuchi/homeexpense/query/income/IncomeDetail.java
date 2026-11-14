package dev.higuchi.homeexpense.query.income;

import java.time.LocalDate;

public record IncomeDetail(
    String id,
    String description,
    int amount,
    LocalDate receiveDate,
    String attributeId,
    String attributeName,
    long version) {}
