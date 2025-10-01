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
public class LoanInterestDto {
    private long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount;
}
