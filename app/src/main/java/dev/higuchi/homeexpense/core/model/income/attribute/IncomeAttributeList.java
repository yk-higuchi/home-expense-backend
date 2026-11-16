package dev.higuchi.homeexpense.core.model.income.attribute;

import dev.higuchi.homeexpense.core.model.AttributeMove;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import java.util.List;
import java.util.Map;

public record IncomeAttributeList(List<IncomeAttribute> attributes) {

  public static final int MAX_SIZE = 100;

  public IncomeAttributeList {
    attributes = List.copyOf(attributes);
  }

  public int size() {
    return attributes.size();
  }

  public IncomeAttributeList moved(List<AttributeMove> moves) {
    return new IncomeAttributeList(AttributeMove.applyAll(attributes, moves));
  }

  public boolean hasSameOrderAs(IncomeAttributeList other) {
    return identifiers().equals(other.identifiers());
  }

  public List<IncomeAttributeIdentifier> identifiers() {
    return attributes.stream().map(IncomeAttribute::incomeAttributeIdentifier).toList();
  }

  public DocumentDigest digest() {
    return DocumentDigest.of(
        Map.of(
            "income_attributes",
            attributes.stream()
                .map(attribute ->
                    Map.of(
                        "id", attribute.incomeAttributeIdentifier().value(),
                        "name", attribute.incomeAttributeName().value()))
                .toList()));
  }
}
