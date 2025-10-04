package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;
import org.tikito.dto.loan.LoanInterestDto;
import org.tikito.dto.loan.LoanType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrUpdateLoanPartRequest {
    private Long id;
    private long loanId;
    @NotBlank
    private String name;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;
    private DateRange dateRange;
    private double amount;
    @NotNull
    private LoanType loanType;
    private long currencyId;
    private List<LoanInterestDto> interests = new ArrayList<>();

    public boolean isNew() {
        return id == null || id == 0;
    }
}
