package org.tikito.entity.security;

import org.tikito.dto.security.AggregatedHistoricalSecurityHoldingValueDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AggregatedHistoricalSecurityHoldingValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "aggregated_historical_security_holding_account",
            joinColumns = @JoinColumn(name = "aggregated_historical_security_holding_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();
    private LocalDate date;
    private double positionValue = 0;
    private double totalDividend = 0;
    private double totalAdministrativeCosts = 0;
    private double totalTaxes = 0;
    private double totalTransactionCosts = 0;
    private double totalCashInvested = 0;
    private double totalCashWithdrawn = 0;
    private double worth = 0;
    private double maxCashInvested = 0;
    private double cashOnHand = 0;

    public AggregatedHistoricalSecurityHoldingValue(final long userId) {
        this.userId = userId;
    }

    public AggregatedHistoricalSecurityHoldingValueDto toDto() {
        return new AggregatedHistoricalSecurityHoldingValueDto(
                id,
                userId,
                accountIds,
                date,
                positionValue,
                totalDividend,
                totalAdministrativeCosts,
                totalTaxes,
                totalTransactionCosts,
                totalCashInvested,
                totalCashWithdrawn,
                worth,
                maxCashInvested,
                cashOnHand);
    }
}
