package dev.higuchi.homeexpense.application.usecase.income;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.AttributeMove;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeList;
import dev.higuchi.homeexpense.core.model.income.attribute.IncomeAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class IncomeAttributeListPatchService {

  IncomeAttributeRepository incomeAttributeRepository;

  public IncomeAttributeListPatchService(IncomeAttributeRepository incomeAttributeRepository) {
    this.incomeAttributeRepository = incomeAttributeRepository;
  }

  public DocumentDigest move(List<AttributeMove> moves, ExpectedDigest expectedDigest) {
    IncomeAttributeList current = incomeAttributeRepository.list();
    incomeAttributeRepository.verifyList(current, expectedDigest);
    IncomeAttributeList moved = current.moved(moves);
    if (!moved.hasSameOrderAs(current)) {
      incomeAttributeRepository.reorder(moved);
    }
    return moved.digest();
  }
}
