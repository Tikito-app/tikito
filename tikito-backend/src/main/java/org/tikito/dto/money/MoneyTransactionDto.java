package org.tikito.dto.money;

import org.tikito.entity.money.MoneyTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Getter
@Setter
@AllArgsConstructor
public class MoneyTransactionDto {
    private Long id;
    private long userId;
    private long accountId;
    private String counterpartyAccountName;
    private String counterpartyAccountNumber;
    private Instant timestamp;
    private double amount;
    private Double finalBalance;
    private String description;
    private long currencyId;
    private Long groupId;
    private Long loanId;
    private double exchangeRate;

    public static String getUniqueKey(final MoneyTransaction transaction) {
        return transaction.getCounterpartyAccountNumber() + "#" +
                LocalDate.ofInstant(transaction.getTimestamp(), ZoneOffset.UTC) + "#" +
                transaction.getAmount() + "#" +
                transaction.getCurrencyId();
    }

    public static String getUniqueKey(final MoneyTransactionImportLine line) {
        return line.getCounterpartyAccountNumber() + "#" +
                LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC) + "#" +
                line.getAmount() + "#" +
                line.getCurrencyId();
    }
}
