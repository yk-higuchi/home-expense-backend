package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.application.usecase.income.IncomeAttributeUpdateCommand;
import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncomeAttributePutRequest(
    @NotBlank(message = "nameは必須入力です")
        @Size(max = IncomeAttributeName.MAX_LENGTH, message = "nameは{max}文字以内で入力してください")
        @JsonProperty("name")
        String name) {

  IncomeAttributeUpdateCommand toCommand(String id, ExpectedVersion expectedVersion) {
    return new IncomeAttributeUpdateCommand(
        new IncomeAttributeIdentifier(id), new IncomeAttributeName(name), expectedVersion);
  }
}
