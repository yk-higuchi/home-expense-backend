package dev.higuchi.homeexpense.application.usecase.expense;

import dev.higuchi.homeexpense.application.transaction.RetryOnSerializationFailure;
import dev.higuchi.homeexpense.core.model.AttributeMove;
import dev.higuchi.homeexpense.core.model.ExpectedDigest;
import dev.higuchi.homeexpense.core.model.document.DocumentDigest;
import dev.higuchi.homeexpense.core.model.expense.ExpenseCategory;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeList;
import dev.higuchi.homeexpense.core.model.expense.attribute.ExpenseAttributeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
@Transactional
@RetryOnSerializationFailure
public class ExpenseAttributeListPatchService {

  ExpenseAttributeRepository expenseAttributeRepository;

  public ExpenseAttributeListPatchService(ExpenseAttributeRepository expenseAttributeRepository) {
    this.expenseAttributeRepository = expenseAttributeRepository;
  }

  public DocumentDigest move(
      ExpenseCategory category, List<AttributeMove> moves, ExpectedDigest expectedDigest) {
    ExpenseAttributeList current = expenseAttributeRepository.list(category);
    expenseAttributeRepository.verifyList(current, expectedDigest);
    ExpenseAttributeList moved = current.moved(moves);
    if (!moved.hasSameOrderAs(current)) {
      expenseAttributeRepository.reorder(moved);
    }
    return moved.digest();
  }
}
