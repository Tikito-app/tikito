package org.tikito.entity.security;

import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;
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
public class HistoricalSecurityHoldingValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "historical_security_holding_account",
            joinColumns = @JoinColumn(name = "historical_security_holding_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();
    private Long securityHoldingId;
    private Long securityId; // todo: can we remove this?
    private LocalDate date;
    private long currencyId; // or this
    private double currencyMultiplier;
    private int amount;
    private double price;
    private double totalDividend;
    private double totalAdministrativeCosts;
    private double totalTaxes;
    private double totalTransactionCosts;
    private double totalCashInvested;
    private double totalCashWithdrawn;
    private double worth = 0;
    private double maxCashInvested = 0;
    private double cashOnHand = 0;

    public HistoricalSecurityHoldingValue(final long userId, final HistoricalSecurityHoldingValueDto dto) {
        this.id = dto.getId();
        this.userId = userId;
        this.accountIds = new HashSet<>(dto.getAccountIds());
        this.securityHoldingId = dto.getSecurityHoldingId();
        this.securityId = dto.getSecurityId();
        this.currencyId = dto.getCurrencyId();
        this.date = dto.getDate();
        this.amount = dto.getAmount();
        this.price = dto.getPrice() * dto.getCurrencyMultiplier();
        this.totalDividend = dto.getTotalDividend() * dto.getCurrencyMultiplier();
        this.totalAdministrativeCosts = dto.getTotalAdministrativeCosts() * dto.getCurrencyMultiplier();
        this.totalTaxes = dto.getTotalTaxes() * dto.getCurrencyMultiplier();
        this.totalTransactionCosts = dto.getTotalTransactionCosts() * dto.getCurrencyMultiplier();
        this.totalCashInvested = dto.getTotalCashInvested() * dto.getCurrencyMultiplier();
        this.totalCashWithdrawn = dto.getTotalCashWithdrawn() * dto.getCurrencyMultiplier();
        this.worth = dto.getWorth() * dto.getCurrencyMultiplier();
        this.maxCashInvested = dto.getMaxCashInvested() * dto.getCurrencyMultiplier();
        this.cashOnHand = dto.getCashOnHand() * dto.getCurrencyMultiplier();
    }

    public HistoricalSecurityHoldingValueDto toDto() {
        return new HistoricalSecurityHoldingValueDto(
                id,
                userId,
                new HashSet<>(accountIds),
                securityHoldingId,
                securityId,
                date,
                currencyId,
                currencyMultiplier,
                amount,
                price,
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
