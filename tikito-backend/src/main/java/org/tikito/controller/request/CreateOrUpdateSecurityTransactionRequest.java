package org.tikito.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.tikito.dto.security.SecurityTransactionType;

import java.time.Instant;

@Getter
@Setter
public class CreateOrUpdateSecurityTransactionRequest extends CreateOrUpdateRequest {
    @NotNull
    private Long accountId;
    private String isin;
    @NotNull
    private Long currencyId;
    @NotNull
    private Integer amount;
    @NotNull
    private Double price;
    private String description;
    @NotNull
    private Instant timestamp;
    private Double cash;
    @NotNull
    private Double exchangeRate;
    @NotNull
    private SecurityTransactionType transactionType;
}
