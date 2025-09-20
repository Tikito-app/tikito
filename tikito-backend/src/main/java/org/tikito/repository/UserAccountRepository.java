package org.tikito.repository;

import org.tikito.entity.UserAccount;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);

    Optional<UserAccount> findByEmail(@NotEmpty String email);

    Optional<UserAccount> findByActivationCode(@NotEmpty String activationCode);
}
