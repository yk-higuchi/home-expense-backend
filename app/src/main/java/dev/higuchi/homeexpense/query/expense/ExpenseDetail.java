package dev.higuchi.homeexpense.query.expense;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.time.LocalDate;

public record ExpenseDetail(
    String id,
    String description,
    int amount,
    LocalDate paymentDate,
    String attributeId,
    String attributeName,
    String category,
    DocumentDigest digest) {}
