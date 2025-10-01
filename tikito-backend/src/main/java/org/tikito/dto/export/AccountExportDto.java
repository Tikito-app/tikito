package org.tikito.dto.export;

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
    private String name;
    private AccountType accountType;
    private String accountNumber;
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
