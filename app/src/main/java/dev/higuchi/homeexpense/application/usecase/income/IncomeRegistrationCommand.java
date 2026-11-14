package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.income.ReceiveDate;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;

public record IncomeRegistrationCommand(
    Description description,
    Amount amount,
    ReceiveDate receiveDate,
    IncomeAttributeIdentifier incomeAttributeIdentifier) {}
