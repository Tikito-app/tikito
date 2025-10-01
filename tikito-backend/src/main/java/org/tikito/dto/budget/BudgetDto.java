package org.tikito.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.money.MoneyTransactionGroupDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private Long id;
    private long userId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetDateRange dateRange;
    private double amount;
    private Set<Long> accountIds;
    private List<MoneyTransactionGroupDto> groups;
}
