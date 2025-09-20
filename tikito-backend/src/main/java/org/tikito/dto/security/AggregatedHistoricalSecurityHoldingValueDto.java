package org.tikito.dto.security;

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
public class AggregatedHistoricalSecurityHoldingValueDto {
    private Long id;
    private long userId;
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
}
