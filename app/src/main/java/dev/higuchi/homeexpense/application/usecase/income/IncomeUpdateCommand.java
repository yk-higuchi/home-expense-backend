package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.income.IncomeIdentifier;
import dev.higuchi.homeexpense.core.model.income.ReceiveDate;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;

public record IncomeUpdateCommand(
    IncomeIdentifier incomeIdentifier,
    Description description,
    Amount amount,
    ReceiveDate receiveDate,
    IncomeAttributeIdentifier incomeAttributeIdentifier,
    ExpectedVersion expectedVersion) {}
