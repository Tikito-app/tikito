package org.tikito.dto.export;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.loan.LoanType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoanPartExportDto {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private double amount;
    private String currency;
    private LoanType loanType;
    private List<LoanInterestExportDto> interests = new ArrayList<>();
}
