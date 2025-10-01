package org.tikito.dto.loan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanPartDto {
    private long id;
    private long loanId;
    private long userId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount;
    private double remainingAmount;
    private double periodicPayment;
    private long currencyId;
    private LoanType loanType;
    private List<LoanInterestDto> interests;
}
