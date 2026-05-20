package org.tikito.dto.security;

import lombok.*;
import org.tikito.entity.security.Security;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityTransactionImportLine {
    private Instant timestamp;
    private String isin;
    private String productName; // filled for new isins
    private String currency;
    private long currencyId;
    private int amount;
    private double price;
    private String description;
    private SecurityTransactionType transactionType;
    private String country;
    private Double cash;
    private Security security;
    private int lineNumber;
    private List<String> cells;
    private boolean failed;
    private String failedReason;
    private Double exchangeRate;

    public SecurityTransactionImportLine(final int lineNumber, final List<String> cells) {
        this.lineNumber = lineNumber;
        this.cells = cells;
    }

    public void setFailedReason(final String failedReason) {
        this.failedReason = failedReason;
        this.failed = true;
    }
}
