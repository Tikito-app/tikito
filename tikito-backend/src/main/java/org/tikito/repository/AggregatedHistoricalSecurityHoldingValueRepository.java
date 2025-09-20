package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.security.AggregatedHistoricalSecurityHoldingValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AggregatedHistoricalSecurityHoldingValueRepository extends JpaRepository<AggregatedHistoricalSecurityHoldingValue, Long> {
    List<AggregatedHistoricalSecurityHoldingValue> findByUserId(long userId);

    @Query("select v from AggregatedHistoricalSecurityHoldingValue v where v.userId = :userId order by v.date desc limit 1")
    Optional<AggregatedHistoricalSecurityHoldingValue> findLatestByUserId(long userId);

    @Modifying
    void deleteAllByUserId(long userId);

    @Modifying
    void deleteByAccountIds(long accountIds);
}
