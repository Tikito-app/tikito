package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.money.HistoricalMoneyHoldingValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricalMoneyHoldingValueRepository extends JpaRepository<HistoricalMoneyHoldingValue, Long> {
    @Modifying
    void deleteByAccountId(long accountId);
}
