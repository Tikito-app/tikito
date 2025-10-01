package org.tikito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.tikito.entity.loan.LoanValue;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface LoanValueRepository extends JpaRepository<LoanValue, Long> {
    @Modifying
    @Query("delete LoanValue v where v.loanPartId in :loanPartIds")
    void deleteByLoanPartIdIn(Set<Long> loanPartIds);

    @Modifying
    @Query("delete LoanValue v where v.loanId = :loanId")
    void deleteByLoanId(long loanId);

    @Modifying
    @Query("delete LoanValue v where v.loanPartId = :loanPartId")
    void deleteByLoanPartId(long loanPartId);

    @Query("select v from LoanValue v where v.loanPartId in :loanPartIds")
    List<LoanValue> findByLoanPartIds(Set<Long> loanPartIds);

    @Query("select v from LoanValue v where v.loanPartId in :loanPartIds and v.date <= :date order by v.date desc")
    List<LoanValue> findByLoanPartIdsAndDateBefore(Set<Long> loanPartIds, LocalDate date);
}
