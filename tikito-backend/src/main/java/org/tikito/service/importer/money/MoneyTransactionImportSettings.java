package org.tikito.service.importer.money;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class MoneyTransactionImportSettings {
    @Builder.Default
    private final int amountColumnIndex = -1;
    @Builder.Default
    private final int counterpartAccountNameColumnIndex = -1;
    @Builder.Default
    private final int counterpartAccountNumberColumnIndex = -1;
    @Builder.Default
    private final int debitCreditColumnIndex = -1;
    private final String debitCreditColumnCreditIndication;
    @Builder.Default
    private final int timestampColumnIndex = -1;
    private final String timestampFormat;
    @Builder.Default
    private final int currencyColumnIndex = -1;
    @Builder.Default
    private final int finalBalanceColumnIndex = -1;
    @Builder.Default
    private final int descriptionColumnIndex = -1;
    private boolean descendingSortOfRows;
    @Builder.Default
    private final int exchangeRateColumnIndex = -1;
}