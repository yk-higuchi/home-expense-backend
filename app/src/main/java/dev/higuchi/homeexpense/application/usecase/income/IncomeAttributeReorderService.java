package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeOrder;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeRepository;
import dev.higuchi.homeexpense.core.model.income.attribute.InvalidIncomeAttributeOrderException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeAttributeReorderService {

  IncomeAttributeRepository incomeAttributeRepository;

  public IncomeAttributeReorderService(IncomeAttributeRepository incomeAttributeRepository) {
    this.incomeAttributeRepository = incomeAttributeRepository;
  }

  public DocumentDigest reorder(IncomeAttributeReorderCommand command) {
    IncomeAttributeOrder requested = command.order();
    IncomeAttributeOrder current = incomeAttributeRepository.order();
    incomeAttributeRepository.verifyOrder(current, command.expectedDigest());
    if (!requested.hasSameMembersAs(current)) {
      throw new InvalidIncomeAttributeOrderException();
    }
    if (!requested.equals(current)) {
      incomeAttributeRepository.reorder(requested);
    }
    return requested.digest();
  }
}
