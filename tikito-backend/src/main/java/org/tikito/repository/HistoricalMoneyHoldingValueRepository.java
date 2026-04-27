package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.money.HistoricalMoneyHoldingValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HistoricalMoneyHoldingValueRepository extends JpaRepository<HistoricalMoneyHoldingValue, Long> {
    @Modifying
    void deleteByAccountId(long accountId);

    @Query("""
            select v from HistoricalMoneyHoldingValue v where
                v.userId = :userId and
                (:accountIds is null or v.accountId in :accountIds) and
                (:currencies is null or v.currencyId in :currencies) and
                (:startDate is null or v.date >= :startDate) and
                (:endDate is null or v.date < :endDate)
                order by v.date asc
            """)
    List<HistoricalMoneyHoldingValue> findByFilter(final long userId, Set<Long> accountIds, Set<Long> currencies, final boolean nonGrouped, LocalDate startDate, LocalDate endDate);

}
