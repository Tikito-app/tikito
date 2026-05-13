package org.tikito.dto.export;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.money.MoneyTransactionImportLine;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class MoneyTransactionExportDto {
    private String accountName;
    private String counterpartyAccountName;
    private String counterpartyAccountNumber;
    private Instant timestamp;
    private double amount;
    private Double finalBalance;
    private String description;
    private String currency;
    private double exchangeRate;

    public MoneyTransactionExportDto(final String accountName,
                                     final String counterpartyAccountName,
                                     final String counterpartyAccountNumber,
                                     final Instant timestamp,
                                     final double amount,
                                     final Double finalBalance,
                                     final String description,
                                     final String currency,
                                     final double exchangeRate) {
        this.accountName = accountName;
        this.counterpartyAccountName = counterpartyAccountName;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.timestamp = timestamp;
        this.amount = amount;
        this.finalBalance = finalBalance;
        this.description = description;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
    }

    public MoneyTransactionImportLine toImportLine() {
        return MoneyTransactionImportLine
                .builder()
                .counterpartyAccountName(counterpartyAccountName)
                .counterpartyAccountNumber(counterpartyAccountNumber)
                .timestamp(timestamp)
                .amount(amount)
                .finalBalance(finalBalance)
                .description(description)
                .currency(currency)
                .exchangeRate(exchangeRate)
                .build();
    }
}
