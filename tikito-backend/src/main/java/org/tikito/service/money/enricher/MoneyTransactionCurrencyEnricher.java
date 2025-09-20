package org.tikito.service.money.enricher;

import org.tikito.dto.money.MoneyTransactionImportLine;

public class MoneyTransactionCurrencyEnricher implements MoneyTransactionEnricher {
    final long currencyId;

    public MoneyTransactionCurrencyEnricher(final long currencyId) {
        this.currencyId = currencyId;
    }

    @Override
    public void enrich(final MoneyTransactionImportLine line) {
        line.setCurrencyId(currencyId);
    }
}
