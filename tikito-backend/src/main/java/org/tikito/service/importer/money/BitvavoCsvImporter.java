package org.tikito.service.importer.money;

import org.springframework.stereotype.Component;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.service.money.enricher.MoneyCryptoEnricher;
import org.tikito.service.money.enricher.MoneyTransactionEnricher;

import java.util.List;

@Component
public class BitvavoCsvImporter extends MoneyTransactionImporter {

    @Override
    public boolean applies(final MoneyTransactionImportResultDto result) {
        return result.getImporter() instanceof BitvavoFileParser;
    }

    @Override
    public List<MoneyTransactionEnricher> getEnrichers() {
        return List.of(
                new MoneyCryptoEnricher()
        );
    }
}
