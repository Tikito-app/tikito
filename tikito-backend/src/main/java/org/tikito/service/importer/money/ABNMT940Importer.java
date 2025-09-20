package org.tikito.service.importer.money;

import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.service.money.enricher.MoneyTransactionEnricher;

import java.util.List;

public class ABNMT940Importer extends MoneyTransactionImporter {

    @Override
    public boolean applies(final MoneyTransactionImportResultDto result) {
        return result.getImporter() == null && result.getFilename().toLowerCase().endsWith(".sta");
    }

    @Override
    public List<MoneyTransactionEnricher> getEnrichers() {
        return List.of();
    }
}
