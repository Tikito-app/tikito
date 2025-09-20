package org.tikito.controller.request;

import org.tikito.dto.AccountType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrUpdateAccountRequest {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String accountNumber;
    @NotNull
    private AccountType accountType;
    private long currencyId;
}
