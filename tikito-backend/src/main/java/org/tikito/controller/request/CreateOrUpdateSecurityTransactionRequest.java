package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.security.SecurityTransactionType;

import java.time.Instant;

@Getter
@Setter
public class CreateOrUpdateSecurityTransactionRequest extends CreateOrUpdateRequest {
    private long accountId;
    private String isin;
    private long currencyId;
    private int amount;
    private double price;
    private String description;
    private Instant timestamp;
    private Double cash;
    private double exchangeRate;
    private SecurityTransactionType transactionType;
}
