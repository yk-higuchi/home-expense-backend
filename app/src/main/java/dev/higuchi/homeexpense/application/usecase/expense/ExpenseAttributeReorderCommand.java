package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeOrder;

public record ExpenseAttributeReorderCommand(
    ExpenseAttributeOrder order, ExpectedDigest expectedDigest) {}
