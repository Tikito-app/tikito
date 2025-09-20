package org.tikito.service.money.enricher;

import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.service.extractor.DateExtractor;

import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

public class MoneyTransactionDateEnricher implements MoneyTransactionEnricher {
    @Override
    public void enrich(final MoneyTransactionImportLine line) {
        final Optional<Instant> instant1 = DateExtractor.extractDateSlashed(line.getDescription());
        final Optional<Instant> instant2 = DateExtractor.extractDateDashed(line.getDescription());

        final Optional<Instant> instant = Stream.of(instant1, instant2)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findAny();

        instant.ifPresent(line::setTimestamp);
    }
}
