package org.tikito.service.money.enricher;

import org.springframework.util.StringUtils;
import org.tikito.dto.money.MoneyTransactionImportLine;

public class MoneyCryptoEnricher implements MoneyTransactionEnricher {
    @Override
    public void enrich(final MoneyTransactionImportLine line) {
        if (!StringUtils.hasText(line.getCounterpartyAccountName())) {
            line.setCounterpartyAccountName(line.getCurrency());
        }
    }
}
