package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.money.HistoricalBudgetValue;

import java.time.LocalDate;
import java.util.List;

public interface HistoricalBudgetValueRepository extends JpaRepository<HistoricalBudgetValue, Long> {

    @Modifying
    void deleteByUserIdAndGroupId(long userId, long groupId);

    @Query("select v from HistoricalBudgetValue v where v.userId = :userId and (:startDate is null or v.date >= :startDate) and (:endDate is null or v.date < :endDate)")
    List<HistoricalBudgetValue> findByUserIdDateBetween(long userId, LocalDate startDate, LocalDate endDate);
}
