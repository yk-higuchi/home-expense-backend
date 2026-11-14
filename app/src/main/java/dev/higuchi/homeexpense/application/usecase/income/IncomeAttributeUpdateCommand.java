package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeName;

public record IncomeAttributeUpdateCommand(
    IncomeAttributeIdentifier incomeAttributeIdentifier,
    IncomeAttributeName incomeAttributeName,
    ExpectedVersion expectedVersion) {}
