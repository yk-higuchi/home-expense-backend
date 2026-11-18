package dev.higuchi.homeexpense.presentation.api.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.expense.ExpenseDetail;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record ExpenseGetResponse(
    @JsonProperty("id") String id,
    @JsonProperty("description") String description,
    @JsonProperty("amount") Integer amount,
    @JsonProperty("payment_date") String paymentDate,
    @JsonProperty("expense_attribute") ExpenseAttributeResponse expenseAttribute) {

  static ExpenseGetResponse from(ExpenseDetail detail) {
    return new ExpenseGetResponse(
        detail.id(),
        detail.description(),
        detail.amount(),
        detail.paymentDate().toString(),
        new ExpenseAttributeResponse(
            detail.attributeId(), detail.attributeName(), detail.category()));
  }
}
