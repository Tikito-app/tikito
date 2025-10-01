package org.tikito.dto.loan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanValueDto {
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
}
