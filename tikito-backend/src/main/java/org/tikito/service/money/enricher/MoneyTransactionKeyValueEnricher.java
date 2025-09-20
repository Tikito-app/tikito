package org.tikito.service.money.enricher;

import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.service.extractor.KeyValueExtractor;

import java.util.Map;

public class MoneyTransactionKeyValueEnricher implements MoneyTransactionEnricher {
    @Override
    public void enrich(final MoneyTransactionImportLine line) {
        final Map<String, String> map = KeyValueExtractor.extract(line.getDescription());
        map.forEach((key, value) -> {
            switch (key) {
                case "NAAM":
                    line.setCounterpartAccountName(value.replaceAll("\r", ""));
                    break;
                case "IBAN":
                    line.setCounterpartAccountNumber(value.replaceAll("\r", ""));
                    break;
                case "OMSCHRIJVING":
                    line.setDescription(value.replaceAll("\r", ""));
                    break;
            }
        });
    }
}
