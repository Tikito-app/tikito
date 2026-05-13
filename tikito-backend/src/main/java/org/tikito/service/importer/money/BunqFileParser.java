package org.tikito.service.importer.money;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BunqFileParser extends MoneyTransactionFileParser {
    private static final List<String> HEADERS = List.of(
            "Date",
            "Interest Date",
            "Amount",
            "Account",
            "Counterparty",
            "Name",
            "Description");

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public MoneyTransactionImportSettings getSettings() {
        return MoneyTransactionImportSettings.builder()
                .descendingSortOfRows(true)
                .amountColumnIndex(2)
                .timestampColumnIndex(0)
                .timestampFormat("yyyy-MM-dd")
                .counterpartyAccountNumberColumnIndex(4)
                .counterpartyAccountNameColumnIndex(5)
                .descriptionColumnIndex(6)
                .build();
    }
}
