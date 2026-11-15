package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeOrder;

public record IncomeAttributeReorderCommand(
    IncomeAttributeOrder order, ExpectedDigest expectedDigest) {}
