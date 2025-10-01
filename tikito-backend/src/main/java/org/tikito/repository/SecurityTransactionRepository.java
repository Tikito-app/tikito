package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.security.SecurityTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SecurityTransactionRepository extends JpaRepository<SecurityTransaction, Long> {
    List<SecurityTransaction> findBySecurityId(Long securityId);

    @Query("select t from SecurityTransaction t where t.securityId in :securityIds " +
            "and (:timestamp is null or t.timestamp >= :timestamp)")
    List<SecurityTransaction> findBySecurityIdIn(Set<Long> securityIds, final Instant timestamp);

    List<SecurityTransaction> findByAccountId(long accountId);

    Optional<SecurityTransaction> findByUserIdAndId(long userId, long id);

    @Modifying
    void deleteByUserIdAndId(long userId, long id);

    @Modifying
    void deleteByAccountId(long accountId);

    @Modifying
    void deleteByUserIdAndSecurityId(long userId, Long securityId);

    @Query("select distinct(t.currencyId) from SecurityTransaction t")
    Set<Long> getCurrencyIdsInUse();

    @Query("select distinct(t.securityId) from SecurityTransaction t where t.userId = :userId and t.securityId is not null")
    Set<Long> findSecurityIdsByUserId(long userId);

    List<SecurityTransaction> findAllByUserId(long userId);
}
