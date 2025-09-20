package org.tikito.service.importer.money;

import java.util.List;
import java.util.Map;

import static org.tikito.service.importer.money.CustomMoneyImportHeaderName.*;

public class CustomMoneyFileParser extends MoneyTransactionFileParser {

    private final Map<String, Integer> headerConfig;
    private final String debitIndication;
    private final String timestampFormat;
    private final String dateFormat;
    private final String timeFormat;

    public CustomMoneyFileParser(final Map<String, Integer> headerConfig, final String debitIndication, final String timestampFormat, final String dateFormat, final String timeFormat) {
        this.headerConfig = headerConfig;
        this.debitIndication = debitIndication;
        this.timestampFormat = timestampFormat;
        this.dateFormat = dateFormat;
        this.timeFormat = timeFormat;
    }

    @Override
    public List<String> getHeaders() {
        return List.of();
    }

    @Override
    public MoneyTransactionImportSettings getSettings() {
        return new MoneyTransactionImportSettings(
                getHeader(AMOUNT),
                getHeader(COUNTERPART_ACCOUNT_NAME),
                getHeader(COUNTERPART_ACCOUNT_NUMBER),
                getHeader(DEBIT_CREDIT),
                debitIndication,
                getHeader(TIMESTAMP),
                timestampFormat,
                getHeader(CURRENCY),
                getHeader(FINAL_BALANCE),
                getHeader(DESCRIPTION),
                false,
                getHeader(EXCHANGE_RATE));
    }

    private int getHeader(final String key) {
        if (headerConfig.containsKey(key)) {
            return headerConfig.get(key);
        }
        return -1;
    }
}
