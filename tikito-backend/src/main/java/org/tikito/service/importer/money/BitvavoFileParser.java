package org.tikito.service.importer.money;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BitvavoFileParser extends MoneyTransactionFileParser {
    private static final List<String> HEADERS = List.of(
            "Timezone",
            "Date",
            "Time",
            "Type",
            "Currency",
            "Amount",
            "Quote Currency",
            "Quote Price",
            "Received / Paid Currency",
            "Received / Paid Amount",
            "Fee currency",
            "Fee amount",
            "Status",
            "Transaction ID",
            "Address");

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public MoneyTransactionImportSettings getSettings() {
        return MoneyTransactionImportSettings.builder()
                .descendingSortOfRows(true)
                .amountColumnIndex(5)
                .timestampColumnIndex(1)
                .timestampFormat("yyyy-MM-dd")
                .timeColumnIndex(2)
                .timeFormat("HH:mm:ss")
                .currencyColumnIndex(4)
                .build();
    }
}
