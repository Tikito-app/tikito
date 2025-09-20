package org.tikito.entity.money;

import org.tikito.dto.money.HistoricalMoneyHoldingValueDto;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HistoricalMoneyHoldingValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private long accountId;
    private LocalDate date;
    private long currencyId;
    private double currencyMultiplier;
    private double amount;
    private boolean manuallySet;

    public HistoricalMoneyHoldingValue(final long userId, final HistoricalMoneyHoldingValueDto dto) {
        this.userId = userId;
        this.accountId = dto.getAccountId();
        this.date = dto.getDate();
        this.currencyId = dto.getCurrencyId();
        this.currencyMultiplier = dto.getCurrencyMultiplier();
        this.amount = dto.getAmount();
    }

    public HistoricalMoneyHoldingValueDto toDto() {
        return new HistoricalMoneyHoldingValueDto(
                accountId,
                date,
                currencyId,
                currencyMultiplier,
                amount);
    }
}
