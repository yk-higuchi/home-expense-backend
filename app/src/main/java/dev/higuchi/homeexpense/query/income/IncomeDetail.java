package dev.higuchi.homeexpense.query.income;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.time.LocalDate;

public record IncomeDetail(
    String id,
    String description,
    int amount,
    LocalDate receiveDate,
    String attributeId,
    String attributeName,
    DocumentDigest digest) {}
