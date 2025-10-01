package org.tikito.dto.export;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class MoneyTransactionExportDto {
    private String accountName;
    private String counterpartAccountName;
    private String counterpartAccountNumber;
    private Instant timestamp;
    private double amount;
    private double finalBalance;
    private String description;
    private String currency;

    public MoneyTransactionExportDto(final String accountName,
                                     final String counterpartAccountName,
                                     final String counterpartAccountNumber,
                                     final Instant timestamp,
                                     final double amount,
                                     final double finalBalance,
                                     final String description,
                                     final String currency) {
        this.accountName = accountName;
        this.counterpartAccountName = counterpartAccountName;
        this.counterpartAccountNumber = counterpartAccountNumber;
        this.timestamp = timestamp;
        this.amount = amount;
        this.finalBalance = finalBalance;
        this.description = description;
        this.currency = currency;


    }
}
