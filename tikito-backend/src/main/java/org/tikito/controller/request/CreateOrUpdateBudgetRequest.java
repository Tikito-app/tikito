package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.DateRange;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class CreateOrUpdateBudgetRequest extends CreateOrUpdateRequest {
    private long groupId;
    private LocalDate startDate;
    private LocalDate endDate;
    private DateRange dateRange;
    private int dateRangeAmount;
    private double amount;
}
