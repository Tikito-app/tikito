package org.tikito.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.security.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SecurityHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "security_holding_account",
            joinColumns = @JoinColumn(name = "security_holding_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();
    private Long securityId;
    @Enumerated(EnumType.STRING)
    private SecurityType securityType;
    private long currencyId;
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

    public SecurityHolding(final long userId, final Set<Long> accountIds, final SecurityTransactionImportLine transaction) {
        this.userId = userId;
        this.accountIds = new HashSet<>(accountIds);
        this.securityId = transaction.getSecurityId();
        this.amount = transaction.getAmount();
        this.securityType = SecurityType.STOCK;
        this.currencyId = transaction.getCurrencyId();
    }

    public SecurityHoldingDto toDto() {
        return toDto(null);
    }

    public SecurityHoldingDto toDto(final SecurityDto security) {
        return new SecurityHoldingDto(
                id,
                userId,
                new HashSet<>(accountIds),
                securityId,
                currencyId,
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
                cashOnHand,
                security);
    }

    public void mutateAmount(final SecurityTransactionImportLine transaction) {
        if (transaction.getTransactionType() == SecurityTransactionType.BUY) {
            this.amount += transaction.getAmount();
        } else if (transaction.getTransactionType() == SecurityTransactionType.SELL) {
            this.amount -= transaction.getAmount();
        }
    }

    public void apply(final HistoricalSecurityHoldingValue historicalSecurityHoldingValue) {
        this.currencyId = historicalSecurityHoldingValue.getCurrencyId(); // we update it here, because the currency can change if the exchange changes
        this.price = historicalSecurityHoldingValue.getPrice();
        this.totalDividend = historicalSecurityHoldingValue.getTotalDividend();
        this.totalAdministrativeCosts = historicalSecurityHoldingValue.getTotalAdministrativeCosts();
        this.totalTaxes = historicalSecurityHoldingValue.getTotalTaxes();
        this.totalTransactionCosts = historicalSecurityHoldingValue.getTotalTransactionCosts();
        this.totalCashInvested = historicalSecurityHoldingValue.getTotalCashInvested();
        this.totalCashWithdrawn = historicalSecurityHoldingValue.getTotalCashWithdrawn();
        this.worth = historicalSecurityHoldingValue.getWorth();
        this.maxCashInvested = historicalSecurityHoldingValue.getMaxCashInvested();
        this.cashOnHand = historicalSecurityHoldingValue.getCashOnHand();
    }
}
