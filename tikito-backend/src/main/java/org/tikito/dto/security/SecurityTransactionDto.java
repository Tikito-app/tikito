package org.tikito.dto.security;

import org.tikito.entity.security.SecurityTransaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityTransactionDto {
    protected Long id;
    protected long userId;
    protected Long securityId;
    protected String isin;
    protected long accountId;
    protected long currencyId;
    protected int amount;
    protected double price;
    protected String description;
    protected Instant timestamp;
    protected SecurityTransactionType transactionType;
    protected Double cash;

    // for front end
    private SecurityDto security;

    public static String getUniqueKey(final SecurityTransaction transaction) {
        return getUniqueKey(
                transaction.getAccountId(),
                transaction.getIsin(),
                transaction.getTimestamp(),
                transaction.getAmount(),
                transaction.getPrice(),
                transaction.getTransactionType(),
                transaction.getCash());
    }

    public static String getUniqueKey(final long accountId, final SecurityTransactionImportLine line) {
        return getUniqueKey(
                accountId,
                line.getIsin(),
                line.getTimestamp(),
                line.getAmount(),
                line.getPrice(),
                line.getTransactionType(),
                line.getCash());
    }

    public static String getUniqueKey(final long accountId,
                                      final String isin,
                                      final Instant timestamp,
                                      final int amount,
                                      final double price,
                                      final SecurityTransactionType transactionType,
                                      final Double cash) {
        return accountId + "-" + emptyToNull(isin) + "-" + timestamp.toString() + "-" + amount + "-" + price + "-" + transactionType.name() + "-" + cash;
    }

    private static String emptyToNull(final String value) {
        return StringUtils.hasText(value) ? value : "null";
    }
}
