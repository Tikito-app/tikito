package org.tikito.entity.budget;

import org.tikito.dto.budget.BudgetDateRange;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.entity.money.MoneyTransactionGroup;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private String name;
    private BudgetDateRange dateRange;
    private double amount;

    @OneToMany(fetch = FetchType.EAGER)
    private List<MoneyTransactionGroup> groups;

    public Budget(final long userId) {
        this.userId = userId;
    }

    public BudgetDto toDto() {
        return new BudgetDto(
                id,
                userId,
                name,
                dateRange,
                amount,
                groups
                        .stream()
                        .map(MoneyTransactionGroup::toDto)
                        .toList());
    }
}
