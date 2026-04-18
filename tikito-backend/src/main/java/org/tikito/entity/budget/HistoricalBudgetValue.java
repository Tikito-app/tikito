package org.tikito.entity.budget;

import org.tikito.dto.budget.HistoricalBudgetValueDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.entity.money.MoneyTransactionGroup;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HistoricalBudgetValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private long groupId;
    private LocalDate date;
    private double budgeted;
    private double spent;

    public HistoricalBudgetValue(final long userId, final MoneyTransactionGroup group, final LocalDate date) {
        this.userId = userId;
        this.groupId = group.getId();
        this.date = date;
        this.budgeted = group.getAmount();
    }

    public HistoricalBudgetValueDto toDto() {
        return new HistoricalBudgetValueDto(
                id,
                userId,
                groupId,
                date,
                budgeted,
                spent);
    }
}
