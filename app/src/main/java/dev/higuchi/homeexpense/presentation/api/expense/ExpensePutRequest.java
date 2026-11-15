package dev.higuchi.homeexpense.presentation.api.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseUpdateCommand;
import dev.higuchi.homeexpense.core.model.Amount;
import dev.higuchi.homeexpense.core.model.Description;
import dev.higuchi.homeexpense.core.model.ExpectedVersion;
import dev.higuchi.homeexpense.core.model.expense.ExpenseIdentifier;
import dev.higuchi.homeexpense.core.model.expense.PaymentDate;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.presentation.validation.IsoLocalDate;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ExpensePutRequest(
    @NotBlank(message = "descriptionは必須入力です")
        @Size(max = Description.MAX_LENGTH, message = "descriptionは{max}文字以内で入力してください")
        @JsonProperty("description")
        String description,
    @NotNull(message = "amountは必須入力です")
        @Positive(message = "amountは正の値でなければなりません")
        @Max(
            value = Amount.MAX_VALUE,
            message = "amountは${formatter.format('%,d', value)}以下で入力してください")
        @JsonProperty("amount")
        Integer amount,
    @NotBlank(message = "payment_dateは必須入力です")
        @IsoLocalDate(message = "payment_dateの形式に誤りがあります")
        @JsonProperty("payment_date")
        String paymentDate,
    @NotBlank(message = "attribute_idは必須入力です")
        @UuidFormat(message = "attribute_idの形式に誤りがあります")
        @JsonProperty("attribute_id")
        String attributeId) {

  ExpenseUpdateCommand toCommand(String id, ExpectedVersion expectedVersion) {
    return new ExpenseUpdateCommand(
        new ExpenseIdentifier(id),
        new Description(description),
        new Amount(amount),
        new PaymentDate(LocalDate.parse(paymentDate)),
        new ExpenseAttributeIdentifier(attributeId),
        expectedVersion);
  }
}
