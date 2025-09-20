package org.tikito.dto.security;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricalSecurityHoldingValueDto {
    private Long id;
    private long userId;
    private Set<Long> accountIds = new HashSet<>();
    private Long securityHoldingId;
    private Long securityId;
    private LocalDate date;
    private long currencyId;
    private double currencyMultiplier;
    private int amount = 0;
    private double price = 0;
    private double totalDividend = 0;
    private double totalAdministrativeCosts = 0;
    private double totalTaxes = 0;
    private double totalTransactionCosts = 0;
    private double totalCashInvested = 0;
    private double totalCashWithdrawn = 0;
    private double worth = 0;
    private double maxCashInvested = 0;
    private double cashOnHand = 0;

    public HistoricalSecurityHoldingValueDto(final LocalDate date, final HistoricalSecurityHoldingValueDto previousValue) {
        this.date = date;
        this.userId = previousValue.getUserId();
        this.accountIds = new HashSet<>(previousValue.getAccountIds());
        this.securityHoldingId = previousValue.getSecurityHoldingId();
        this.securityId = previousValue.getSecurityId();
        this.currencyId = previousValue.getCurrencyId();
        this.currencyMultiplier = previousValue.getCurrencyMultiplier();
        this.amount = previousValue.getAmount();
        this.price = previousValue.getPrice();
        this.totalDividend = previousValue.getTotalDividend();
        this.totalAdministrativeCosts = previousValue.getTotalAdministrativeCosts();
        this.totalTaxes = previousValue.getTotalTaxes();
        this.totalTransactionCosts = previousValue.getTotalTransactionCosts();
        this.totalCashInvested = previousValue.getTotalCashInvested();
        this.totalCashWithdrawn = previousValue.getTotalCashWithdrawn();
        this.worth = previousValue.getWorth();
        this.maxCashInvested = previousValue.getMaxCashInvested();
        this.cashOnHand = previousValue.getCashOnHand();
    }

    public HistoricalSecurityHoldingValueDto(final Set<Long> accountIds, final Long securityId, final Long securityHoldingId, final long currencyId) {
        this.accountIds = new HashSet<>(accountIds);
        this.securityId = securityId;
        this.securityHoldingId = securityHoldingId;
        this.currencyId = currencyId;
    }
}
