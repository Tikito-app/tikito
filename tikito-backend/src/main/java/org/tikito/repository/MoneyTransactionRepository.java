package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.money.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Long> {
    List<MoneyTransaction> findByAccountId(long accountId);

    @Query("""
            select t from MoneyTransaction t where
                t.userId = :userId and
                (:accountIds is null or t.accountId in :accountIds) and
                (:startDate is null or t.timestamp >= :startDate) and
                (:endDate is null or t.timestamp < :endDate)
                order by t.timestamp asc
            """)
    List<MoneyTransaction> findByFilter(final long userId, Set<Long> accountIds, Set<Long> groupIds, final boolean nonGrouped, Instant startDate, Instant endDate);

    @Modifying
    void deleteByUserIdAndId(long userId, long transactionId);

    @Modifying
    void deleteByAccountId(long accountId);

    Optional<MoneyTransaction> findByUserIdAndId(long userId, long id);

    @Query("select distinct(t.currencyId) from MoneyTransaction t")
    Set<Long> getCurrencyIdsInUse();
}
