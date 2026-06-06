package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.security.SecurityHolding;

import java.util.List;
import java.util.Optional;

public interface SecurityHoldingRepository extends JpaRepository<SecurityHolding, Long> {
    List<SecurityHolding> findByUserId(long userId);

    Optional<SecurityHolding> findByUserIdAndId(long userId, long securityHoldingId);

    @Query("select h from SecurityHolding h where h.userId = :userId and (:accountId is null or h.accountId = :accountId)")
    List<SecurityHolding> findByUserIdAndAccountId(long userId, Long accountId);

    @Modifying
    void deleteByAccountId(long accountId);

    List<SecurityHolding> findByUserIdAndSecurityId(long userId, long securityId);
}
