package org.tikito.service.importer.money;

import java.util.List;

public class INGFileParser extends MoneyTransactionFileParser {
    private static final List<String> HEADERS = List.of(
            "Date",
            "Name / Description",
            "Account",
            "Counterparty",
            "Code",
            "Debit/credit",
            "Amount (EUR)",
            "Transaction type",
            "Notifications",
            "Resulting balance",
            "Tag");


    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public MoneyTransactionImportSettings getSettings() {
        return MoneyTransactionImportSettings.builder()
                .descendingSortOfRows(true)
                .amountColumnIndex(6)
                .timestampColumnIndex(0)
                .timestampFormat("yyyyMMdd")
                .finalBalanceColumnIndex(9)
                .descriptionColumnIndex(8)
                .counterpartAccountNameColumnIndex(1)
                .counterpartAccountNumberColumnIndex(3)
                .debitCreditColumnIndex(5)
                .debitCreditColumnCreditIndication("Credit")
//                .currencyColumnIndex(1)
                .build();
    }
}
