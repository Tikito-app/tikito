package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.budget.Budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserId(long userId);

    Optional<Budget> findByUserIdAndId(long userId, long budgetId);

    @Modifying
    void deleteByUserIdAndId(long userId, long budgetId);

    @Query("""
            select b from Budget b where
                        b.userId = :userId and
                        (b.startDate >= :startDate and b.startDate <= :endDate) or
                        (b.endDate is null or (b.endDate >= :startDate and b.endDate <= :endDate))
            """)
    List<Budget> findByUserIdAndDateBetween(long userId, LocalDate startDate, LocalDate endDate);
}
