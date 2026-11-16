package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;

public record ExpenseAttributeRegistrationCommand(
    ExpenseAttributeName expenseAttributeName, ExpenseCategory expenseCategory) {}
