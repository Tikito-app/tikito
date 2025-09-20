package org.tikito.controller.request;

import org.tikito.dto.budget.BudgetDateRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrUpdateBudgetRequest {
    private Long id;
    private String name;
    private BudgetDateRange dateRange;
    private double amount;
    private List<Long> groupIds;

    public boolean isNew() {
        return id == null;
    }
}
