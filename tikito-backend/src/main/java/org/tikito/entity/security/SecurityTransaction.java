package org.tikito.entity.security;

import org.tikito.dto.export.SecurityTransactionExportDto;
import org.tikito.dto.security.SecurityTransactionDto;
import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private Long securityId;
    private String isin;
    private long accountId;
    private long currencyId;
    private int amount;
    private double price;
    private String description;
    private Instant timestamp;
    private Double cash;
    private double exchangeRate;

    @Enumerated(EnumType.STRING)
    private SecurityTransactionType transactionType;

    public SecurityTransaction(final long userId, final Long accountId, final SecurityTransactionImportLine line) {
        this.userId = userId;
        this.securityId = line.getSecurity() == null ? null : line.getSecurity().getId();
        this.isin = line.getIsin();
        this.accountId = accountId;
        this.currencyId = line.getCurrencyId();
        this.amount = line.getAmount();
        this.price = line.getPrice() * line.getExchangeRate();
        this.description = line.getDescription();
        this.timestamp = line.getTimestamp();
        this.transactionType = line.getTransactionType();
        this.cash = line.getCash() == null ? null : line.getCash() * line.getExchangeRate();
        this.exchangeRate = line.getExchangeRate();
    }

    public SecurityTransaction(final long userId, final long accountId, final Long securityId, final long currencyId, final SecurityTransactionExportDto dto) {
        this.userId = userId;
        this.securityId = securityId;
        this.accountId = accountId;
        this.currencyId = currencyId;
        this.amount = dto.getAmount();
        this.price = dto.getPrice();
        this.description = dto.getDescription();
        this.timestamp = dto.getTimestamp();
        this.cash = dto.getCash();
        this.exchangeRate = dto.getExchangeRate();
        this.transactionType = dto.getTransactionType();
    }

    public SecurityTransactionDto toDto() {
        return new SecurityTransactionDto(
                id,
                userId,
                securityId,
                isin,
                accountId,
                currencyId,
                amount,
                price,
                description,
                timestamp,
                transactionType,
                cash,
                null);
    }

    public SecurityTransactionExportDto toExportDto(final String accountName, final String currency) {
        return new SecurityTransactionExportDto(
                isin,
                accountName,
                currency,
                amount,
                price,
                description,
                timestamp,
                cash,
                exchangeRate,
                transactionType);
    }
}
