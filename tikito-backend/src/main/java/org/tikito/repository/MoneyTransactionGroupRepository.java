package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MoneyTransactionGroupRepository extends JpaRepository<MoneyTransactionGroup, Long> {
    List<MoneyTransactionGroup> findByUserId(long userId);

    Optional<MoneyTransactionGroup> findByUserIdAndId(long userId, long id);

    @Modifying
    void deleteByUserIdAndId(long userId, long groupId);

    @Query("select g from MoneyTransactionGroup g where g.id in :ids or g.loan.id = :loanId")
    List<MoneyTransactionGroup> findAllByIdsOrLoanId(Set<Long> ids, long loanId);

    @Query("select g from MoneyTransactionGroup g where g.id in :ids or g.budget.id = :budgetId")
    List<MoneyTransactionGroup> findAllByIdsOrBudgetId(Set<Long> ids, long budgetId);
}
