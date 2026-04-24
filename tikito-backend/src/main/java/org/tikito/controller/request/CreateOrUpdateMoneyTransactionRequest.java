package org.tikito.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateOrUpdateMoneyTransactionRequest extends CreateOrUpdateRequest {
    private long userId;
    private long accountId;
    private String counterpartyAccountName;
    private String counterpartyAccountNumber;
    private Instant timestamp;
    private double amount;
    private Double finalBalance;
    private String description;
    private long currencyId;
    private Long groupId;
    private Long loanId;
    private double exchangeRate;
}
