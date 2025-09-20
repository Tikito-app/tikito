package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.security.SecurityHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SecurityHoldingRepository extends JpaRepository<SecurityHolding, Long> {
    List<SecurityHolding> findByUserId(long userId);

    Optional<SecurityHolding> findByUserIdAndId(long userId, long securityHoldingId);

    List<SecurityHolding> findByUserIdAndIdIn(long userId, Set<Long> holdingIds);

    List<SecurityHolding> findByUserIdAndSecurityId(long userId, long securityId);

    @Modifying
    void deleteByAccountIds(long accountIds);
}
