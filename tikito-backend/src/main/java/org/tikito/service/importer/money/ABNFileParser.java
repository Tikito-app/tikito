package org.tikito.service.importer.money;

import java.util.List;

public class ABNFileParser extends MoneyTransactionFileParser {
    private static final List<String> HEADERS = List.of(
            "Rekeningnummer",
            "Muntsoort",
            "Transactiedatum",
            "Rentedatum",
            "Beginsaldo",
            "Eindsaldo",
            "Transactiebedrag",
            "Omschrijving");

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public MoneyTransactionImportSettings getSettings() {
        return MoneyTransactionImportSettings.builder()
                .descendingSortOfRows(false)
                .amountColumnIndex(6)
                .timestampColumnIndex(2)
                .timestampFormat("yyyyMMdd")
                .finalBalanceColumnIndex(5)
                .descriptionColumnIndex(7)
                .currencyColumnIndex(1)
                .build();
    }
}
