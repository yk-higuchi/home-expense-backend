package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.query.expense.attribute.ExpenseAttributeDetail;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;

@RegisterForReflection
public record ExpenseAttributeGetListResponse(
    @JsonProperty("expense_attributes") List<ExpenseAttributeGetResponse> list) {

  public static ExpenseAttributeGetListResponse from(List<ExpenseAttributeDetail> details) {
    return new ExpenseAttributeGetListResponse(
        details.stream().map(ExpenseAttributeGetResponse::from).toList());
  }
}
