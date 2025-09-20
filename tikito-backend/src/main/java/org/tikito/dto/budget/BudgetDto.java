package org.tikito.dto.budget;

import org.tikito.dto.money.MoneyTransactionGroupDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDto {
    private Long id;
    private long userId;
    private String name;
    private BudgetDateRange dateRange;
    private double amount;
    private List<MoneyTransactionGroupDto> groups;
}
