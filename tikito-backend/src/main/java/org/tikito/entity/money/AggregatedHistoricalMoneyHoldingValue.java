package org.tikito.entity.money;

import org.tikito.dto.money.AggregatedHistoricalMoneyHoldingValueDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AggregatedHistoricalMoneyHoldingValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "aggregated_historical_money_holding_account",
            joinColumns = @JoinColumn(name = "aggregated_historical_money_holding_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();
    private LocalDate date;
    private double amount;

    public AggregatedHistoricalMoneyHoldingValue(final long userId) {
        this.userId = userId;
    }

    public AggregatedHistoricalMoneyHoldingValueDto toDto() {
        return new AggregatedHistoricalMoneyHoldingValueDto(
                accountIds,
                date,
                amount);
    }
}
