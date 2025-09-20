package org.tikito.dto.money;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalMoneyHoldingValueDto {
    private long accountId;
    private LocalDate date;
    private long currencyId;
    private double currencyMultiplier;
    private double amount;

    public HistoricalMoneyHoldingValueDto(final LocalDate date, final HistoricalMoneyHoldingValueDto previousValue) {
        this.date = date;
        this.accountId = previousValue.accountId;
        this.currencyId = previousValue.getCurrencyId();
        this.amount = previousValue.getAmount();
        this.currencyMultiplier = previousValue.getCurrencyMultiplier();
    }

    public HistoricalMoneyHoldingValueDto(final long accountId, final long currencyId) {
        this.accountId = accountId;
        this.currencyId = currencyId;
    }
}
