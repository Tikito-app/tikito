package org.tikito.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.export.ImportExportSettings;
import org.tikito.dto.export.TikitoExportDto;
import org.tikito.entity.Account;
import org.tikito.service.BaseIntegrationTest;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class ImportExportServiceTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testImportExport() throws IOException {
        loginWithDefaultUser();
        withDefaultCurrencies();

        final String json = getClassPathResource("import-export/tikito.export.json");
        final TikitoExportDto importDto = objectMapper.readValue(json, TikitoExportDto.class);
        final ImportExportSettings settings = getSettings();

        importExportService.importFrom(DEFAULT_USER_ACCOUNT.getId(), importDto, settings);

        final Set<Long> accountIds = accountRepository.findAll().stream().map(Account::getId).collect(Collectors.toSet());
        final TikitoExportDto exportDto = importExportService.export(DEFAULT_USER_ACCOUNT.getId(), accountIds, settings);
        assertEquals(
                objectMapper.writeValueAsString(importDto),
                objectMapper.writeValueAsString(exportDto));
    }

    private static ImportExportSettings getSettings() {
        final ImportExportSettings settings = new ImportExportSettings();

        settings.setAccounts(true);
        settings.setMoneyTransactions(true);
        settings.setSecurityTransactions(true);
        settings.setMoneyTransactionGroups(true);
        settings.setLoans(true);
        return settings;
    }
}
