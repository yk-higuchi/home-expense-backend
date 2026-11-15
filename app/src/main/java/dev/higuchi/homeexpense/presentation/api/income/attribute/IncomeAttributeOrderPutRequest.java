package dev.higuchi.homeexpense.presentation.api.income.attribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.higuchi.homeexpense.application.usecase.income.IncomeAttributeReorderCommand;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeOrder;
import dev.higuchi.homeexpense.presentation.validation.UuidFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.UniqueElements;

public record IncomeAttributeOrderPutRequest(
    @NotNull(message = "idsは必須入力です")
        @Size(max = IncomeAttributeOrder.MAX_SIZE, message = "idsは{max}件以内で指定してください")
        @UniqueElements(message = "idsに同じIDを複数含めることはできません")
        @JsonProperty("ids")
        List<@NotNull(message = "idsにnullは指定できません") @UuidFormat String> ids) {

  IncomeAttributeReorderCommand toCommand(ExpectedDigest expectedDigest) {
    return new IncomeAttributeReorderCommand(
        new IncomeAttributeOrder(ids.stream().map(IncomeAttributeIdentifier::new).toList()),
        expectedDigest);
  }
}
