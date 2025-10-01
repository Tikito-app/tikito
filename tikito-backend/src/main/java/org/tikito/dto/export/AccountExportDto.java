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
    private long currencyId;

    private List<SecurityTransactionExportDto> securityTransactions;
    private List<MoneyTransactionExportDto> moneyTransactions;
}
