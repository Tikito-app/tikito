package org.tikito.dto.money;

import org.tikito.entity.money.MoneyTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

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

    public static String getUniqueKey(final MoneyTransaction transaction) {
        return transaction.getCounterpartAccountName() + "#" +
                transaction.getCounterpartAccountNumber() + "#" +
                transaction.getTimestamp() + "#" +
                transaction.getFinalBalance() + "#" +
                transaction.getAmount() + "#" +
                transaction.getCurrencyId();
    }

    public static String getUniqueKey(final MoneyTransactionImportLine line) {
        return line.getCounterpartAccountName() + "#" +
                line.getCounterpartAccountNumber() + "#" +
                line.getTimestamp() + "#" +
                line.getFinalBalance() + "#" +
                line.getAmount() + "#" +
                line.getCurrencyId();
    }
}
