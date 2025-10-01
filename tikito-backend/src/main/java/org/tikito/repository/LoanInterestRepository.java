package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tikito.entity.loan.LoanInterest;

public interface LoanInterestRepository extends JpaRepository<LoanInterest, Long> {
}
