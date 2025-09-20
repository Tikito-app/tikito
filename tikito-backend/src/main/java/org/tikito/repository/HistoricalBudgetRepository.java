package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.budget.HistoricalBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricalBudgetRepository extends JpaRepository<HistoricalBudget, Long> {

    @Modifying
    void deleteByUserIdAndBudgetId(long userId, long budgetId);

    List<HistoricalBudget> findByUserId(long userId);
}
