package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;

public record ExpenseAttributeUpdateCommand(
    ExpenseAttributeIdentifier expenseAttributeIdentifier,
    ExpenseAttributeName expenseAttributeName,
    ExpectedVersion expectedVersion) {}
