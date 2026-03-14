package org.tikito.dto.export;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionType;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SecurityTransactionExportDto {
    private String isin;
    private String accountName;
    private String currency;
    private int amount;
    private double price;
    private String description;
    private Instant timestamp;
    private Double cash;
    private double exchangeRate;
    private SecurityTransactionType transactionType;

    public SecurityTransactionImportLine toImportLine() {
        return SecurityTransactionImportLine
                .builder()
                .isin(isin)
                .currency(currency)
                .amount(amount)
                .price(price / exchangeRate)
                .description(description)
                .timestamp(timestamp)
                .cash(cash != null ? cash / exchangeRate : null)
                .exchangeRate(exchangeRate)
                .transactionType(transactionType)
                .build();
    }
}
