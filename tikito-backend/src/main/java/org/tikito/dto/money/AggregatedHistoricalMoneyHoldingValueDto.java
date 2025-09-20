package org.tikito.dto.money;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class AggregatedHistoricalMoneyHoldingValueDto {
    private Set<Long> accountIds = new HashSet<>();
    private LocalDate date;
    private double amount;

    public AggregatedHistoricalMoneyHoldingValueDto(final Set<Long> accountIds,
                                                    final LocalDate date,
                                                    final double amount) {
        this.accountIds = new HashSet<>(accountIds);
        this.date = date;
        this.amount = amount;
    }
}
