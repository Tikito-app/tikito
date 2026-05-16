package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.security.SecurityHolding;

import java.util.List;
import java.util.Optional;

public interface SecurityHoldingRepository extends JpaRepository<SecurityHolding, Long> {
    List<SecurityHolding> findByUserId(long userId);

    Optional<SecurityHolding> findByUserIdAndId(long userId, long securityHoldingId);

    List<SecurityHolding> findByUserIdAndAccountId(long userId, long accountId);

    @Modifying
    void deleteByAccountId(long accountId);

    List<SecurityHolding> findByUserIdAndSecurityId(long userId, long securityId);
}
