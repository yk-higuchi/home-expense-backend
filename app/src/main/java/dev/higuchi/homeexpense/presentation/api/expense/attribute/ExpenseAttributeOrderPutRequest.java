package dev.higuchi.homeexpense.presentation.api.expense.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.application.usecase.expense.ExpenseAttributeReorderCommand;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeOrder;
import dev.higuchi.homeexpense.presentation.validation.ExpenseCategory;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.UniqueElements;

public record ExpenseAttributeOrderPutRequest(
    @NotNull(message = "categoryは必須入力です") @ExpenseCategory @JsonProperty("category")
        String category,
    @NotNull(message = "idsは必須入力です")
        @Size(max = ExpenseAttributeOrder.MAX_SIZE, message = "idsは{max}件以内で指定してください")
        @UniqueElements(message = "idsに同じIDを複数含めることはできません")
        @JsonProperty("ids")
        List<@NotNull(message = "idsにnullは指定できません") @UuidFormat String> ids) {

  ExpenseAttributeReorderCommand toCommand(ExpectedDigest expectedDigest) {
    return new ExpenseAttributeReorderCommand(
        new ExpenseAttributeOrder(
            dev.higuchi.homeexpense.core.model.expense.ExpenseCategory.valueOf(category),
            ids.stream().map(ExpenseAttributeIdentifier::new).toList()),
        expectedDigest);
  }
}
