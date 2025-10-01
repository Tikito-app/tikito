package org.tikito.dto.money;

import lombok.*;
import org.tikito.dto.AccountDto;
import org.tikito.dto.DebitCredit;
import org.tikito.service.MT940.MT940Transaction;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneyTransactionImportLine {
    private String counterpartAccountNumber;
    private String counterpartAccountName;
    private Instant timestamp;
    private DebitCredit debitCredit;
    private String code;
    private double amount;
    private double finalBalance;
    private long currencyId;
    private String currency;
    private String transactionType;
    private String description;
    private int lineNumber;
    private List<String> cells;
    private MT940Transaction mt940Transaction;
    private boolean failed;
    private String failedReason;
    private Double exchangeRate;

    public MoneyTransactionImportLine(final int lineNumber, final List<String> cells) {
        this.lineNumber = lineNumber;
        this.cells = cells;
    }

    public MoneyTransactionImportLine(final AccountDto account, final MT940Transaction transaction, final int lineNumber) {
        this.mt940Transaction = transaction;
        this.counterpartAccountNumber = transaction.getToAccountNumber();
        this.counterpartAccountName = transaction.getToAccountName();
        this.timestamp = transaction.getDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        this.debitCredit = transaction.getAmount() < 0 ? DebitCredit.DEBIT : DebitCredit.CREDIT;
        this.amount = transaction.getAmount();
        this.description = transaction.getDescription();
        this.lineNumber = lineNumber;
    }

    public void setFailedReason(final String failedReason) {
        this.failedReason = failedReason;
        this.failed = true;
    }
}
