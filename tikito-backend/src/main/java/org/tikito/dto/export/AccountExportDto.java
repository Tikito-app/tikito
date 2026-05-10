package org.tikito.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountExportDto {
    @NotNull
    private String name;
    private String accountNumber;
    @NotNull
    private String currency;

    public AccountExportDto(final String name, final String accountNumber, final String currency) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.currency = currency;
    }

    private List<SecurityTransactionExportDto> securityTransactions;
    private List<MoneyTransactionExportDto> moneyTransactions;
}
