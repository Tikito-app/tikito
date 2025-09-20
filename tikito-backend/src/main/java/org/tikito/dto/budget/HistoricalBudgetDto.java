package org.tikito.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class HistoricalBudgetDto {
    private Long id;
    private long userId;
    private long budgetId;
    private LocalDate date;
    private double budgeted;
    private double spent;
}
