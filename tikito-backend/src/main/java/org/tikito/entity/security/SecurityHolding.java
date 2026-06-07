package org.tikito.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.security.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SecurityHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private Long accountId;
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

    public SecurityHolding(final long userId, final Long accountId, final SecurityTransactionImportLine transaction) {
        this.userId = userId;
        this.accountId = accountId;
        this.securityId = transaction.getSecurity().getId();
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
                accountId,
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

    /**
     * Applies the latest historical holding value and reverts the exchange rate. This means that the prices, costs etc
     * are persisted in the original currency.
     */
    public void apply(final HistoricalSecurityHoldingValue historicalSecurityHoldingValue) {
        this.currencyId = historicalSecurityHoldingValue.getCurrencyId(); // we update it here, because the currency can change if the exchange changes
        this.price = historicalSecurityHoldingValue.getPrice() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.totalDividend = historicalSecurityHoldingValue.getTotalDividend() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.totalAdministrativeCosts = historicalSecurityHoldingValue.getTotalAdministrativeCosts() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.totalTaxes = historicalSecurityHoldingValue.getTotalTaxes() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.totalTransactionCosts = historicalSecurityHoldingValue.getTotalTransactionCosts() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.totalCashInvested = historicalSecurityHoldingValue.getTotalCashInvested() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.totalCashWithdrawn = historicalSecurityHoldingValue.getTotalCashWithdrawn() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.worth = historicalSecurityHoldingValue.getWorth() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.maxCashInvested = historicalSecurityHoldingValue.getMaxCashInvested() / historicalSecurityHoldingValue.getCurrencyMultiplier();
        this.cashOnHand = historicalSecurityHoldingValue.getCashOnHand() / historicalSecurityHoldingValue.getCurrencyMultiplier();
    }
}
