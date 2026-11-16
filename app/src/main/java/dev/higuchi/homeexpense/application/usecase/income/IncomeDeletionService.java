package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.income.Income;
import dev.higuchi.homeexpense.core.model.income.IncomeIdentifier;
import dev.higuchi.homeexpense.core.model.income.IncomeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeDeletionService {

  IncomeRepository incomeRepository;

  public IncomeDeletionService(IncomeRepository incomeRepository) {
    this.incomeRepository = incomeRepository;
  }

  public void delete(IncomeIdentifier incomeIdentifier, ExpectedDigest expectedDigest) {
    Income income = incomeRepository.get(incomeIdentifier);
    incomeRepository.delete(income, expectedDigest);
  }
}
