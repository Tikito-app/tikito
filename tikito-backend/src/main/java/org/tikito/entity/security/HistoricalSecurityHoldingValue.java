package org.tikito.entity.security;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HistoricalSecurityHoldingValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private Long accountId;
    private Long securityHoldingId;
    private Long securityId; // todo: can we remove this?
    private LocalDate date;
    private long currencyId; // or this
    private double exchangeRate;
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
        this.accountId = dto.getAccountId();
        this.securityHoldingId = dto.getSecurityHoldingId();
        this.securityId = dto.getSecurityId();
        this.currencyId = dto.getCurrencyId();
        this.date = dto.getDate();
        this.amount = dto.getAmount();
        this.price = dto.getPrice() * dto.getExchangeRate();
        this.totalDividend = dto.getTotalDividend() * dto.getExchangeRate();
        this.totalAdministrativeCosts = dto.getTotalAdministrativeCosts() * dto.getExchangeRate();
        this.totalTaxes = dto.getTotalTaxes() * dto.getExchangeRate();
        this.totalTransactionCosts = dto.getTotalTransactionCosts() * dto.getExchangeRate();
        this.totalCashInvested = dto.getTotalCashInvested() * dto.getExchangeRate();
        this.totalCashWithdrawn = dto.getTotalCashWithdrawn() * dto.getExchangeRate();
        this.worth = dto.getWorth() * dto.getExchangeRate();
        this.maxCashInvested = dto.getMaxCashInvested() * dto.getExchangeRate();
        this.cashOnHand = dto.getCashOnHand() * dto.getExchangeRate();
        this.exchangeRate = dto.getExchangeRate();
    }

    public HistoricalSecurityHoldingValueDto toDto() {
        return new HistoricalSecurityHoldingValueDto(
                id,
                userId,
                accountId,
                securityHoldingId,
                securityId,
                date,
                currencyId,
                exchangeRate,
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
