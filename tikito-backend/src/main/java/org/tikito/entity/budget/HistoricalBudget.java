package org.tikito.entity.budget;

import org.tikito.dto.budget.HistoricalBudgetDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HistoricalBudget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private long budgetId;
    private LocalDate date;
    private double budgeted;
    private double spent;

    public HistoricalBudgetDto toDto() {
        return new HistoricalBudgetDto(
                id,
                userId,
                budgetId,
                date,
                budgeted,
                spent);
    }
}
