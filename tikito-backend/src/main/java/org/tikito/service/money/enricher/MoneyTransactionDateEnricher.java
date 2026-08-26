package org.tikito.service.money.enricher;

import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.service.extractor.DateExtractor;

import java.time.Instant;
import java.util.Optional;

public class MoneyTransactionDateEnricher implements MoneyTransactionEnricher {
    @Override
    public void enrich(final MoneyTransactionImportLine line) {
        final Optional<Instant> instant = DateExtractor.extractDate(line.getDescription());

        instant.ifPresent(line::setTimestamp);
    }
}
