package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoneyTransactionGroupRepository extends JpaRepository<MoneyTransactionGroup, Long> {
    List<MoneyTransactionGroup> findByUserId(long userId);

    Optional<MoneyTransactionGroup> findByUserIdAndId(long userId, long id);

    @Modifying
    void deleteByUserIdAndId(long userId, long groupId);
}
