package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.tikito.entity.loan.Loan;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUserId(long userId);

    Optional<Loan> findByUserIdAndId(long userId, long loanId);

    @Modifying
    void deleteByUserIdAndId(long userId, long loanId);
}
