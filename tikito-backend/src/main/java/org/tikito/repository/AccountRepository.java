package org.tikito.repository;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.tikito.dto.AccountType;
import org.tikito.entity.Account;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserIdAndId(long userId, long accountId);

    @Modifying
    void deleteByUserIdAndId(long userId, long accountId);

    List<Account> findByUserId(long userId);

    List<Account> findByUserIdAndAccountType(long userId, AccountType accountType);

    @Query("select a from Account a where a.userId = :userId AND a.name in :names")
    List<Account> findByUserIdAndName(long userId, Set<String> names);

    @Query("select count(a) from Account a where a.userId = :userId and a.id != :accountId and a.accountNumber = :accountNumber")
    int countByAccountNumberAndNotMyId(long userId, @NotBlank String accountNumber, Long accountId);
}
