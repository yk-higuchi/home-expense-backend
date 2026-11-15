package dev.higuchi.homeexpense.query.expense.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;

public record ExpenseAttributeDetail(
    String id, String name, String category, DocumentDigest digest) {}
