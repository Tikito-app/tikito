package org.tikito.entity.budget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.budget.BudgetDateRange;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.entity.money.MoneyTransactionGroup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private LocalDate startDate;
    private LocalDate endDate;
    private BudgetDateRange dateRange;
    private double amount;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "budget_account",
            joinColumns = @JoinColumn(name = "budget_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();

    @OneToMany(mappedBy = "budget", fetch = FetchType.EAGER)
    private List<MoneyTransactionGroup> groups = new ArrayList<>();

    public Budget(final long userId) {
        this.userId = userId;
    }

    public BudgetDto toDto() {
        return new BudgetDto(
                id,
                userId,
                name,
                startDate,
                endDate,
                dateRange,
                amount,
                accountIds,
                groups
                        .stream()
                        .map(MoneyTransactionGroup::toDto)
                        .toList());
    }
}
