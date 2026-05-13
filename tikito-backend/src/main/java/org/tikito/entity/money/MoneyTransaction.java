package org.tikito.entity.money;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.MoneyTransactionExportDto;
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
    private String counterpartyAccountName;
    private String counterpartyAccountNumber;
    private Long counterpartyAccountId;
    private Instant timestamp;
    private double amount;
    private Double finalBalance;
    private String description;
    private long currencyId;
    private Long groupId;
    private Long loanId;
    private double exchangeRate;

    public MoneyTransaction(final long userId, final long accountId, final MoneyTransactionImportLine line) {
        this.userId = userId;
        this.accountId = accountId;
        this.counterpartyAccountName = line.getCounterpartyAccountName();
        this.counterpartyAccountNumber = line.getCounterpartyAccountNumber();
        this.timestamp = line.getTimestamp();
        this.amount = line.getAmount();
        this.finalBalance = line.getFinalBalance() == null ? null : line.getFinalBalance() * line.getExchangeRate();
        this.description = line.getDescription();
        this.currencyId = line.getCurrencyId();
        this.exchangeRate = line.getExchangeRate();
    }

    public MoneyTransaction(final long userId, final long accountId, final long currencyId, final MoneyTransactionExportDto dto) {
        this.userId = userId;
        this.accountId = accountId;
        this.counterpartyAccountName = dto.getCounterpartyAccountName();
        this.counterpartyAccountNumber = dto.getCounterpartyAccountNumber();
//        this.counterpartyAccountId = dto.getCounterpartyAccountName(; // todo
        this.timestamp = dto.getTimestamp();
        this.amount = dto.getAmount();
        this.finalBalance = dto.getFinalBalance();
        this.description = dto.getDescription();
        this.currencyId = currencyId;
        // todo: groupID, etc
    }

    public MoneyTransaction(final long userId) {
        this.userId = userId;
    }

    public MoneyTransactionDto toDto() {
        return new MoneyTransactionDto(
                id,
                userId,
                accountId,
                counterpartyAccountName,
                counterpartyAccountNumber,
                timestamp,
                amount,
                finalBalance,
                description,
                currencyId,
                groupId,
                loanId,
                exchangeRate);
    }

    public MoneyTransactionExportDto toExportDto(final String accountName, final String currency) {
        return new MoneyTransactionExportDto(
                accountName,
                counterpartyAccountName,
                counterpartyAccountNumber,
                timestamp,
                amount,
                finalBalance,
                description,
                currency,
                exchangeRate
//                groupId, // todo
//                loanId
        );
    }

    public double getNormalizedAmount() {
        return amount * exchangeRate;
    }
}
