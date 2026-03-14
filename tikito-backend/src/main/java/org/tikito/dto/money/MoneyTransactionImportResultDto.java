package org.tikito.dto.money;

import org.tikito.dto.AccountDto;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.service.MT940.MT940Transaction;
import org.tikito.service.importer.money.MoneyTransactionFileParser;
import org.tikito.service.importer.money.MoneyTransactionImportSettings;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class MoneyTransactionImportResultDto {
    public static final String FAILED_NO_KNOWN_CURRENCY = "No known currency";
    public static final String FAILED_NO_TRANSACTION_TYPE = "No transaction type";
    public static final String FAILED_NO_VALID_TIMESTAMP = "Cannot extract timestamp";
    public static final String FAILED_NO_EXCHANGE_RATE = "Cannot extract exchanger rate";
    public static final String FAILED_NO_PRICE = "Cannot extract price";
    public static final String FAILED_DUPLICATE_TRANSACTION = "Duplicate transaction";
    public static final String FAILED_EXPECTED_BUY_NEW_ISIN = "Expected buy transaction for a new isin";
    public static final String FAILED_EXPECTED_BUY_NEW_ISIN_SAME_TIMESTAMP = "Expected next transaction has the same timestamp";
    public static final String FAILED_INVALID_LINE = "Invalid line";

    private final List<MoneyTransactionImportLine> lines;
    private final List<MoneyTransaction> importedTransactions = new ArrayList<>();
    private final MoneyTransactionImportSettings settings;
    private final MoneyTransactionFileParser importer;
    private final String filename;
//    private final Map<Long, SecurityHolding> existingSecurityHoldings = new HashMap<>();

    public MoneyTransactionImportResultDto(final List<List<String>> csv,
                                           final MoneyTransactionImportSettings settings,
                                           final MoneyTransactionFileParser importer,
                                           final String filename) {
        this.settings = settings;
        this.importer = importer;
        this.filename = filename;

        lines = new ArrayList<>();
        for (int i = 0; i < csv.size(); i++) {
            lines.add(new MoneyTransactionImportLine(i, csv.get(i)));
        }
    }

    public MoneyTransactionImportResultDto(final AccountDto account,
                                           final List<MT940Transaction> transactions,
                                           final String filename) {
        final AtomicInteger count = new AtomicInteger(1);
        this.settings = null;
        this.importer = null;
        this.filename = filename;
        this.lines = transactions
                .stream()
                .map(t -> new MoneyTransactionImportLine(account, t, count.getAndIncrement()))
                .toList();
    }

    public MoneyTransactionImportResultDto(final List<MoneyTransactionImportLine> lines) {
        this.lines = new ArrayList<>(lines);
        this.settings = null;
        this.importer = null;
        this.filename = null;
    }
}
