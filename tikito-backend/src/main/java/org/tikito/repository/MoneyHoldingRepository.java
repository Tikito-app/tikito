package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.money.MoneyHolding;

import java.util.List;
import java.util.Optional;

public interface MoneyHoldingRepository extends JpaRepository<MoneyHolding, Long> {
    @Modifying
    void deleteByUserIdAndAccountId(long userId, long accountId);

    Optional<MoneyHolding> findByUserIdAndAccountId(long userId, long accountId);

    List<MoneyHolding> findByUserId(long userId);
}
