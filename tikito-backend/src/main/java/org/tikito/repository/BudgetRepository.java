package org.tikito.repository;

import org.tikito.entity.budget.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(long userId);

    Optional<Budget> findByUserIdAndId(long userId, long budgetId);
}
