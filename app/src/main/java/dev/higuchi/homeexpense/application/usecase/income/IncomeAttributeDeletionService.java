package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.IncomeRepository;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttribute;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeConstraintException;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeIdentifier;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeAttributeDeletionService {

  IncomeAttributeRepository incomeAttributeRepository;
  IncomeRepository incomeRepository;

  public IncomeAttributeDeletionService(
      IncomeAttributeRepository incomeAttributeRepository, IncomeRepository incomeRepository) {
    this.incomeAttributeRepository = incomeAttributeRepository;
    this.incomeRepository = incomeRepository;
  }

  public void delete(
      IncomeAttributeIdentifier incomeAttributeIdentifier, ExpectedDigest expectedDigest) {
    IncomeAttribute attribute = incomeAttributeRepository.get(incomeAttributeIdentifier);
    if (incomeRepository.existsByAttributeIdentifier(attribute.incomeAttributeIdentifier())) {
      throw new IncomeAttributeConstraintException();
    }
    incomeAttributeRepository.delete(attribute, expectedDigest);
  }
}
