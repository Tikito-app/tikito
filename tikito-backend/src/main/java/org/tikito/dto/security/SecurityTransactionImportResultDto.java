package org.tikito.dto.security;

import org.tikito.entity.security.SecurityHolding;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.entity.security.Security;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SecurityTransactionImportResultDto {
    public static final String FAILED_NO_KNOWN_CURRENCY = "No known currency";
    public static final String FAILED_NO_EXCHANGE_RATE = "No exchange rate available";
    public static final String FAILED_NO_TRANSACTION_TYPE = "No transaction type";
    public static final String FAILED_NO_VALID_TIMESTAMP = "Cannot extract timestamp";
    public static final String FAILED_NO_PRICE = "Cannot extract price";
    public static final String FAILED_NO_TRANSACTION_COST = "Cannot extract transaction cost";
    public static final String FAILED_NO_ADMIN_COST = "Cannot extract admin cost";
    public static final String FAILED_DUPLICATE_TRANSACTION = "Duplicate transaction";
    public static final String FAILED_EXPECTED_BUY_ISIN_CHANGE = "Expected buy transaction for a new isin";
    public static final String FAILED_EXPECTED_BUY_NEW_ISIN_SAME_TIMESTAMP = "Expected next transaction has the same timestamp";
    public static final String FAILED_INVALID_LINE = "Invalid line";
    public static final String FAILED_ISIN_CHANGE_SAME_ISIN = "Expected a different isin, because it should be changed";
    public static final String FAILED_NO_AMOUNT = "Could not extract amount";

    private final List<SecurityTransactionImportLine> lines;
    private final Map<String, Security> newSecuritiesByIsin = new HashMap<>();
    private final List<SecurityHolding> newSecurityHoldings = new ArrayList<>();
    private final List<SecurityTransaction> importedTransactions = new ArrayList<>();
    private final Map<Long, SecurityHolding> existingSecurityHoldings = new HashMap<>();

    public SecurityTransactionImportResultDto(final List<List<String>> csv) {
        lines = new ArrayList<>();
        for (int i = 0; i < csv.size(); i++) {
            lines.add(new SecurityTransactionImportLine(i, csv.get(i)));
        }
    }
}
