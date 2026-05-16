package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.security.HistoricalSecurityHoldingValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HistoricalSecurityHoldingValueRepository extends JpaRepository<HistoricalSecurityHoldingValue, Long> {
    @Query("""
             select h from HistoricalSecurityHoldingValue h where
                        h.securityId in :securityIds and
                        (:accountIds is null or h.accountId in :accountIds) and
                        h.userId = :userId and
                        (:startDate is null or h.date >= :startDate)
            """)
    List<HistoricalSecurityHoldingValue> findAllBySecurityAndAccount(long userId, Set<Long> accountIds, Set<Long> securityIds, LocalDate startDate);

    @Modifying
    void deleteAllBySecurityHoldingId(Long securityHoldingId);

    @Modifying
    void deleteByAccountId(long accountId);
}
