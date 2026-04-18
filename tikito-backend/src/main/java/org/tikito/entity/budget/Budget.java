package org.tikito.entity.budget;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;
import org.tikito.dto.budget.BudgetDto;
import org.tikito.entity.money.MoneyTransactionGroup;

import java.time.LocalDate;
import java.util.HashSet;
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
    @Enumerated(EnumType.STRING)
    private DateRange dateRange;
    private int dateRangeAmount; // -1 for infinite
    private double amount;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "budget_account",
            joinColumns = @JoinColumn(name = "budget_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER)
    private MoneyTransactionGroup moneyTransactionGroup;

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
                dateRangeAmount,
                amount,
                accountIds,
                moneyTransactionGroup.toDto());
    }
}
