package org.tikito.service.money.enricher;

import org.tikito.dto.money.MoneyTransactionImportLine;

public interface MoneyTransactionEnricher {
    void enrich(MoneyTransactionImportLine line);
}
