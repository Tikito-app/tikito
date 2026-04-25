package org.tikito.dto.export;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.AccountType;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountExportDto {
    @NotNull
    private String name;
    @NotNull
    private AccountType accountType;
    private String accountNumber;
    @NotNull
    private String currency;

    public AccountExportDto(final String name, final AccountType accountType, final String accountNumber, final String currency) {
        this.name = name;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
        this.currency = currency;
    }

    private List<SecurityTransactionExportDto> securityTransactions;
    private List<MoneyTransactionExportDto> moneyTransactions;
}
