package org.tikito.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateOrUpdateMoneyTransactionRequest extends CreateOrUpdateRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long accountId;
    private String counterpartyAccountName;
    private String counterpartyAccountNumber;
    @NotNull
    private Instant timestamp;
    @NotNull
    private Double amount;
    private Double finalBalance;
    private String description;
    @NotNull
    private Long currencyId;
    private Long groupId;
    private Long loanId;
    @NotNull
    private Double exchangeRate;
}
