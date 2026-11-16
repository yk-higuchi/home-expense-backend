package dev.higuchi.homeexpense.query.income.attribute;

import dev.higuchi.homeexpense.core.model.document.DocumentDigest;

public record IncomeAttributeDetail(String id, String name, DocumentDigest digest) {}
