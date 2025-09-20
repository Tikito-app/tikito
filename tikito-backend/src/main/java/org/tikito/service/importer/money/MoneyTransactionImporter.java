package org.tikito.service.importer.money;

import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.service.money.enricher.MoneyTransactionEnricher;

import java.util.List;

public abstract class MoneyTransactionImporter {
    public abstract List<MoneyTransactionEnricher> getEnrichers();

    public abstract boolean applies(final MoneyTransactionImportResultDto result);

    public void apply(final MoneyTransactionImportResultDto result) {
        result.getLines().forEach(line ->
                getEnrichers().forEach(enricher ->
                        enricher.enrich(line)));
    }
}
