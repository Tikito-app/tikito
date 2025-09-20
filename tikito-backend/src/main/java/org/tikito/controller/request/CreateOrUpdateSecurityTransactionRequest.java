package org.tikito.controller.request;

import org.tikito.dto.security.SecurityTransactionType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateOrUpdateSecurityTransactionRequest {
    private Long id;
    private Long securityId;
    private String isin;
    private Long accountId;
    private String currency;
    private int amount;
    private double price;
    private double extraCosts;
    private String description;
    private Instant timestamp;
    private SecurityTransactionType transactionType;
}
