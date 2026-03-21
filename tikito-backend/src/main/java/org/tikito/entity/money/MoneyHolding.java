package org.tikito.entity.money;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.money.HistoricalMoneyHoldingValueDto;
import org.tikito.dto.money.MoneyHoldingDto;

@Getter
@Setter
@Entity
public class MoneyHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private long accountId;
    private long currencyId;
    private double amount;
    private double amountOffset;

    public MoneyHoldingDto toDto() {
        final MoneyHoldingDto dto = new MoneyHoldingDto();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setAccountId(accountId);
        dto.setCurrencyId(currencyId);
        dto.setAmount(amount);
        dto.setAmountOffset(amountOffset);
        return dto;
    }

    public void mutate(final HistoricalMoneyHoldingValueDto value) {
        amount = value.getAmount();
    }
}
