package org.tikito.entity.loan;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.loan.LoanValueDto;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class LoanValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long loanId;
    private long loanPartId;
    private LocalDate date;
    private double amountRemaining;
    private double interestPaid;
    private double interestPaidThisPeriod;
    private double interestRemaining;
    private double repaymentRemaining;
    private double loanPaid;
    private double loanPaidThisPeriod;
    private boolean simulated;

    public LoanValue(final LoanValue value, final LocalDate date) {
        this.loanId = value.getLoanId();
        this.loanPartId = value.getLoanPartId();
        this.amountRemaining = value.getAmountRemaining();
        this.interestRemaining = value.getInterestRemaining();
        this.interestPaid = value.getInterestPaid();
        this.repaymentRemaining = value.getRepaymentRemaining();
        this.loanPaid = value.getLoanPaid();
        this.date = date;
        this.simulated = value.isSimulated();
    }

    public LoanValueDto toDto() {
        return new LoanValueDto(
                id,
                loanId,
                loanPartId,
                date,
                amountRemaining,
                interestPaid,
                interestPaidThisPeriod,
                interestRemaining,
                repaymentRemaining,
                loanPaid,
                loanPaidThisPeriod,
                simulated);
    }
}
