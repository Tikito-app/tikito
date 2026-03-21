package org.tikito.controller.request;

import jakarta.validation.constraints.NotBlank;
import org.tikito.dto.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrUpdateAccountRequest extends CreateOrUpdateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String accountNumber;
    @NotNull
    private AccountType accountType;
    private long currencyId;
}
