package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.budget.BudgetDateRange;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CreateOrUpdateBudgetRequest extends CreateOrUpdateRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetDateRange dateRange;
    private double amount;
    private Set<Long> accountIds;
    private Set<Long> groupIds = new HashSet<>();
}
