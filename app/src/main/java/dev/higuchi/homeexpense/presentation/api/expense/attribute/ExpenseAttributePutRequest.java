package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeUpdateCommand;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExpenseAttributePutRequest(
    @NotBlank(message = "nameは必須入力です")
        @Size(max = ExpenseAttributeName.MAX_LENGTH, message = "nameは{max}文字以内で入力してください")
        @JsonProperty("name")
        String name) {

  ExpenseAttributeUpdateCommand toCommand(String id, ExpectedDigest expectedDigest) {
    return new ExpenseAttributeUpdateCommand(
        new ExpenseAttributeIdentifier(id), new ExpenseAttributeName(name), expectedDigest);
  }
}
