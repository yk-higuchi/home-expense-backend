package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseIdentifier;
import dev.higuchi.homeexpense.core.model.expense.PaymentDate;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;

public record ExpenseUpdateCommand(
    ExpenseIdentifier expenseIdentifier,
    Description description,
    Amount amount,
    PaymentDate paymentDate,
    ExpenseAttributeIdentifier expenseAttributeIdentifier,
    ExpectedDigest expectedDigest) {}
