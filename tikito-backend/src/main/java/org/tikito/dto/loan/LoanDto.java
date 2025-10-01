package org.tikito.dto.loan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;
import org.tikito.dto.money.MoneyTransactionGroupDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private long id;
    private long userId;
    private DateRange dateRange;
    private String name;
    private List<MoneyTransactionGroupDto> groups;
    private List<LoanPartDto> loanParts;
}
