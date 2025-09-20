package org.tikito.service.money.enricher;

import org.tikito.service.MT940.MT940Transaction;
import org.tikito.service.extractor.DateExtractor;
import org.tikito.service.extractor.KeyValueExtractor;
import org.tikito.service.extractor.TRTPExtractor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class MT940Enricher {
    public static void enrich(final List<MT940Transaction> blocks) {
        blocks.forEach(MT940Enricher::enrich);
    }

    private static void enrich(final MT940Transaction transaction) {
        final Map<String, String> map = KeyValueExtractor.extract(transaction.getDescription());
        map.putAll(TRTPExtractor.extract(transaction.getDescription()));
        map.forEach((key, value) -> {
            switch (key) {
                case "NAAM":
                case "NAME":
                    transaction.setToAccountName(value.replaceAll("\r", ""));
                    break;
                case "IBAN":
                    transaction.setToAccountNumber(value.replaceAll("\r", ""));
                    break;
                case "OMSCHRIJVING":
                    transaction.setDescription(value.replaceAll("\r", ""));
                    break;
            }
        });
        // todo
        final Optional<Instant> instant = DateExtractor.extractDateSlashed(transaction.getDescription());
    }
}
