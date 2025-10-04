package org.tikito.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class CreateOrUpdateBudgetRequest {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private DateRange dateRange;
    private int dateRangeAmount;
    private double amount;
    private Set<Long> accountIds;
    private Set<Long> groupIds = new HashSet<>();

    public boolean isNew() {
        return id == null || id == 0;
    }
}
