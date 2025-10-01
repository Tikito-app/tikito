package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.loan.LoanPart;

import java.util.List;
import java.util.Optional;

public interface LoanPartRepository extends JpaRepository<LoanPart, Long> {
    List<LoanPart> findByUserId(long userId);

    Optional<LoanPart> findByUserIdAndId(long userId, long loanId);

    @Modifying
    @Query("delete LoanPart t where t.userId = :userId and t.id = :loanPartId")
    void deleteByUserIdAndId(long userId, long loanPartId);
}
