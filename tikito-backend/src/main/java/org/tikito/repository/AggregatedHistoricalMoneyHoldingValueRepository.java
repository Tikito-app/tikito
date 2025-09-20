package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.money.AggregatedHistoricalMoneyHoldingValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AggregatedHistoricalMoneyHoldingValueRepository extends JpaRepository<AggregatedHistoricalMoneyHoldingValue, Long> {
    @Modifying
    void deleteAllByUserId(long userId);

    List<AggregatedHistoricalMoneyHoldingValue> findAllByUserId(Long id);

    @Modifying
    void deleteByAccountIds(long accountIds);
}
