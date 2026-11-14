package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeRegistrationCommand;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeName;
import dev.higuchi.homeexpense.presentation.validation.ExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExpenseAttributePostRequest(
    @NotBlank(message = "nameは必須入力です")
        @Size(max = ExpenseAttributeName.MAX_LENGTH, message = "nameは{max}文字以内で入力してください")
        @JsonProperty("name")
        String name,
    @NotBlank(message = "categoryは必須入力です") @ExpenseCategory @JsonProperty("category")
        String category) {

  ExpenseAttributeRegistrationCommand toCommand() {
    return new ExpenseAttributeRegistrationCommand(
        new ExpenseAttributeName(name),
        dev.higuchi.homeexpense.core.model.expense.ExpenseCategory.of(category));
  }
}
