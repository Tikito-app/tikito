package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.budget.HistoricalBudgetValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricalBudgetValueRepository extends JpaRepository<HistoricalBudgetValue, Long> {

    @Modifying
    void deleteByUserIdAndBudgetId(long userId, long budgetId);

    List<HistoricalBudgetValue> findByUserId(long userId);
}
