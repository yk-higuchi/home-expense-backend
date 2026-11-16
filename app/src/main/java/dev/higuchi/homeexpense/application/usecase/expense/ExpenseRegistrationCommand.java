package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.expense.PaymentDate;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;

public record ExpenseRegistrationCommand(
    Description description,
    Amount amount,
    PaymentDate paymentDate,
    ExpenseAttributeIdentifier expenseAttributeIdentifier) {}
