package org.tikito.dto.money;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoneyHoldingDto {
    private Long id;
    private long userId;
    private long accountId;
    private long currencyId;
    private double amount;
    private double amountOffset;
}
