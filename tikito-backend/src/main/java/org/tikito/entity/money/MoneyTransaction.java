package org.tikito.entity.money;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.MoneyTransactionExportDto;
import org.tikito.dto.export.TikitoExportDto;
import org.tikito.dto.money.MoneyTransactionDto;
import org.tikito.dto.money.MoneyTransactionImportLine;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class MoneyTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private long accountId;
    private String counterpartAccountName;
    private String counterpartAccountNumber;
    private Long counterpartAccountId;
    private Instant timestamp;
    private double amount;
    private double finalBalance;
    private String description;
    private long currencyId;
    private Long groupId;
    private Long budgetId;
    private Long loanId;
    private double exchangeRate;

    public MoneyTransaction(final long userId, final long accountId, final MoneyTransactionImportLine line) {
        this.userId = userId;
        this.accountId = accountId;
        this.counterpartAccountName = line.getCounterpartAccountName();
        this.counterpartAccountNumber = line.getCounterpartAccountNumber();
        this.timestamp = line.getTimestamp();
        this.amount = line.getAmount() * line.getExchangeRate();
        this.finalBalance = line.getFinalBalance() * line.getExchangeRate();
        this.description = line.getDescription();
        this.currencyId = line.getCurrencyId();
        this.exchangeRate = line.getExchangeRate();
    }

    public MoneyTransaction(final long userId, final long accountId, final long currencyId, final MoneyTransactionExportDto dto) {
        this.userId = userId;
        this.accountId = accountId;
        this.counterpartAccountName = dto.getCounterpartAccountName();
        this.counterpartAccountNumber = dto.getCounterpartAccountNumber();
//        this.counterpartAccountId = dto.getCounterpartAccountName(; // todo
        this.timestamp = dto.getTimestamp();
        this.amount = dto.getAmount();
        this.finalBalance = dto.getFinalBalance();
        this.description = dto.getDescription();
        this.currencyId = currencyId;
        // todo: groupID, etc
    }

    public MoneyTransactionDto toDto() {
        return new MoneyTransactionDto(
                id,
                userId,
                accountId,
                counterpartAccountName,
                counterpartAccountNumber,
                timestamp,
                amount,
                finalBalance,
                description,
                currencyId,
                groupId,
                budgetId,
                loanId);
    }

    public MoneyTransactionExportDto toExportDto(final String accountName, final String currency) {
        return new MoneyTransactionExportDto(
                accountName,
                counterpartAccountName,
                counterpartAccountNumber,
                timestamp,
                amount,
                finalBalance,
                description,
                currency
//                groupId, // todo
//                budgetId,
//                loanId
        );
    }
}
