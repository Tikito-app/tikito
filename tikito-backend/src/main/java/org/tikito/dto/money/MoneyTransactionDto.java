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
    private String counterpartAccountName;
    private String counterpartAccountNumber;
    private Instant timestamp;
    private double amount;
    private double finalBalance;
    private String description;
    private long currencyId;
    private Long groupId;
    private Long budgetId;
    private Long loanId;

    public static String getUniqueKey(final MoneyTransaction transaction) {
        return transaction.getCounterpartAccountNumber() + "#" +
                LocalDate.ofInstant(transaction.getTimestamp(), ZoneOffset.UTC) + "#" +
                transaction.getAmount() + "#" +
                transaction.getCurrencyId();
    }

    public static String getUniqueKey(final MoneyTransactionImportLine line) {
        return line.getCounterpartAccountNumber() + "#" +
                LocalDate.ofInstant(line.getTimestamp(), ZoneOffset.UTC) + "#" +
                line.getAmount() + "#" +
                line.getCurrencyId();
    }
}
