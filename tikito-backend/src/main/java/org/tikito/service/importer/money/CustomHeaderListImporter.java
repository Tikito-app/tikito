package org.tikito.service.importer.money;

import org.springframework.stereotype.Component;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.service.money.enricher.MoneyTransactionDateEnricher;
import org.tikito.service.money.enricher.MoneyTransactionEnricher;
import org.tikito.service.money.enricher.MoneyTransactionKeyValueEnricher;

import java.util.List;

@Component
public class CustomHeaderListImporter extends MoneyTransactionImporter {

    @Override
    public boolean applies(final MoneyTransactionImportResultDto result) {
        return result.getImporter() instanceof CustomMoneyFileParser;
    }

    @Override
    public List<MoneyTransactionEnricher> getEnrichers() {
        return List.of(
                new MoneyTransactionKeyValueEnricher(),
                new MoneyTransactionDateEnricher()
        );
    }
}
