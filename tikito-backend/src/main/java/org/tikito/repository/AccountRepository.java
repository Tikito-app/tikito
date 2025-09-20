package org.tikito.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.tikito.dto.AccountType;
import org.tikito.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserIdAndId(long userId, long accountId);

    @Modifying
    void deleteByUserIdAndId(long userId, long accountId);

    List<Account> findByUserId(long userId);

    List<Account> findByUserIdAndAccountType(long userId, AccountType accountType);
}
