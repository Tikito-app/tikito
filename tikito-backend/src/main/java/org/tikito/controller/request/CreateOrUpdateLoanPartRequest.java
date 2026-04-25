package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.loan.LoanInterestDto;
import org.tikito.dto.loan.LoanType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateOrUpdateLoanPartRequest extends CreateOrUpdateRequest {
    @NotNull
    private Long loanId;
    @NotBlank
    private String name;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Double amount;
    @NotNull
    private LoanType loanType;
    @NotNull
    private Long currencyId;
    private List<LoanInterestDto> interests = new ArrayList<>();
    private Double repaymentAmount;
}
