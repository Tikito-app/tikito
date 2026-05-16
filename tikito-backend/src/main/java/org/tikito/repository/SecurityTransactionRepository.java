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
    @Query(value = """
            select t from SecurityTransaction t where
                        t.userId = :userId and
                        (:securityIds is null or t.securityId in :securityIds) and
                        (t.accountId in :accountIds) and
                        (:timestamp is null or t.timestamp >= :timestamp)
            """)
    List<SecurityTransaction> findBySecurityIdIn(final long userId, Set<Long> securityIds, Set<Long> accountIds, Instant timestamp);

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

    List<SecurityTransaction> findAllByUserId(long userId);

    List<SecurityTransaction> findBySecurityIdAndAccountId(long securityId, long accountId);
}
