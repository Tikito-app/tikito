package org.tikito.service.security;

import org.tikito.dto.ImportSettings;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.entity.security.SecurityHolding;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.exception.UnsupportedImportFormatException;
import org.tikito.service.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.tikito.dto.security.SecurityTransactionImportResultDto.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SecurityTransactionServiceTest extends BaseIntegrationTest {

    @Autowired
    SecurityTransactionService securityTransactionService;

    private Map<String, Integer> headerConfig;
    private final String buyValue = "";
    private final String timestampFormat = "";
    private final String dateFormat = "";
    private final String timeFormat = "";

    @BeforeEach
    public void setup() {
        loginWithDefaultUser();
        withDefaultCurrencies();
        withDefaultAccounts();

        WOLTER_KLUWER = withExistingSecurity("WOLTERS KLUWER", CURRENCY_EURO_ID, new ArrayList<>(List.of(
                isin(ISIN_ONE_OLD, TWENTY_YEARS_AGO, null, "WKL.AS"))));
    }

    @Test
    void testImportNonExistingCurrency() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-non-existing-currency.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertLineFailed(result, FAILED_NO_KNOWN_CURRENCY);
    }

    @Test
    void testImportNoTransactionType() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-no-transaction-type.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertLineFailed(result, FAILED_NO_TRANSACTION_TYPE);
    }

    @Test
    void testImportNoValidTimestampDate() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-no-valid-timestamp-date.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertLineFailed(result, FAILED_NO_VALID_TIMESTAMP);
    }

    @Test
    void testImportNoValidTimestampTime() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-no-valid-timestamp-time.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertLineFailed(result, FAILED_NO_VALID_TIMESTAMP);
    }

    @Test
    void testImportNoValidPrice() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-no-price.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertLineFailed(result, FAILED_NO_PRICE);
    }

    @Test
    void testImportDuplicateTransaction() throws IOException, UnsupportedImportFormatException {
        final SecurityTransaction transaction = getSecurityTransaction();
        securityTransactionRepository.saveAndFlush(transaction);
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-duplicate-transaction.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(4, result.getLines().size());
        assertEquals(3, result.getImportedTransactions().size());
        assertEquals(1, result.getLines().stream().filter(line -> FAILED_DUPLICATE_TRANSACTION.equals(line.getFailedReason())).count());

        final SecurityTransactionImportResultDto newRsult = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(4, newRsult.getLines().size());
        assertEquals(0, newRsult.getImportedTransactions().size());
    }

    @Test
    void testImportNewAndExistingSecurity() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-new-and-existing-trading-company.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(1, result.getNewSecuritiesByIsin().size());
        assertTrue(result.getNewSecuritiesByIsin().containsKey("NL0011540547"));
        final Security security = result.getNewSecuritiesByIsin().get("NL0011540547");
        assertEquals(List.of("NL0011540547"), security.getIsins().stream().map(Isin::getIsin).toList());
        assertEquals("ABN AMRO BANK NV", security.getName());
    }

    @Test
    void testImportIsinChange() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-import-existing-company-with-new-isin.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        final List<SecurityTransaction> importedTransactions = result.getImportedTransactions();

        assertEquals(5, result.getLines().size());
        assertEquals(5, importedTransactions.size());

        final Security security = securityRepository.findAll()
                .stream()
                .filter(s -> "ROYAL DUTCH SHELL A".equals(s.getName()))
                .toList()
                .getFirst();
        final List<Isin> isins = security.getIsins();
        assertEquals(2, isins.size());
        final Isin firstIsin = isins.getFirst();
        final Isin secondIsin = isins.get(1);

        assertEquals("GB00B03MLX29", firstIsin.getIsin());
        assertNull(firstIsin.getValidFrom());
        assertEquals(LocalDate.of(2022, 1, 30), firstIsin.getValidTo());

        assertEquals("GB00BP6MXD84", secondIsin.getIsin());
        assertEquals(LocalDate.of(2022, 1, 31), secondIsin.getValidFrom());
        assertNull(secondIsin.getValidTo());
    }

    @Test
    void testImportNewAndExistingSecurityHolding() throws IOException, UnsupportedImportFormatException {
        final SecurityHolding securityHolding = new SecurityHolding();
        securityHolding.setAccountIds(new HashSet<>());
        securityHolding.getAccountIds().add(DEFAULT_ACCOUNT.getId());
        securityHolding.setSecurityType(SecurityType.STOCK);
        securityHolding.setSecurityId(WOLTER_KLUWER.getId());
        securityHolding.setAmount(5);
        securityHoldingRepository.saveAndFlush(securityHolding);

        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-new-and-existing-security-holding.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(1, result.getNewSecurityHoldings().size());
        assertNotEquals(WOLTER_KLUWER.getId(), result.getNewSecurityHoldings().getFirst().getSecurityId());
    }

    @Test
    void testImportExistingHoldingResultsInZeroAssets() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-holding-results-in-zero-assets.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(1, result.getNewSecurityHoldings().size());
        assertEquals(0, result.getNewSecurityHoldings().getFirst().getAmount());
    }

    @Test
    void testImportNewHoldingResultsInZeroAssets() throws IOException, UnsupportedImportFormatException {
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-new-holding-results-in-zero-assets.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(1, result.getNewSecurityHoldings().size());
        assertEquals(0, result.getNewSecurityHoldings().getFirst().getAmount());
    }

    @Test
    void testImportIsinChangeWithoutIsinChanged() throws IOException, UnsupportedImportFormatException {
        final String oldIsin = "GB00B03MLX29";
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-import-isin-change-with-same-isin.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(1, result.getNewSecurityHoldings().size());
        assertEquals(1, result.getNewSecuritiesByIsin().size());
        assertTrue(result.getNewSecuritiesByIsin().containsKey(oldIsin));

        final SecurityHolding holding = result.getNewSecurityHoldings().getFirst();
        assertEquals(65, holding.getAmount());

        assertTrue(result.getNewSecuritiesByIsin().containsKey(oldIsin));
        final Security company = result.getNewSecuritiesByIsin().get(oldIsin);

        assertEquals(1, company.getIsins().size());
    }

    @Test
    void testImportExistingCompanyProductChangeNoIsinChange() throws IOException, UnsupportedImportFormatException {
        final String oldIsin = "GB00B03MLX29";
        final MockMultipartFile file = getClassPathResourceToImport("security/degiro-account-import-product-change-no-new-isin.csv", "Account.csv");
        final SecurityTransactionImportResultDto result = securityTransactionService.importTransactions(DEFAULT_USER_ACCOUNT.getId(), new ImportSettings(true, true), DEFAULT_ACCOUNT.getId(), file, ';', '"', false, headerConfig, buyValue, timestampFormat, dateFormat, timeFormat);
        assertEquals(1, result.getNewSecurityHoldings().size());
        assertEquals(1, result.getNewSecuritiesByIsin().size());
        assertTrue(result.getNewSecuritiesByIsin().containsKey(oldIsin));

        final SecurityHolding holding = result.getNewSecurityHoldings().getFirst();
        assertEquals(65, holding.getAmount());

        assertTrue(result.getNewSecuritiesByIsin().containsKey(oldIsin));
        final Security company = result.getNewSecuritiesByIsin().get(oldIsin);

        assertEquals(1, company.getIsins().size());
    }

    private void assertLineFailed(final SecurityTransactionImportResultDto result, final String expectedFailedReason) {
        assertEquals(1, result.getLines().size());
        assertTrue(result.getLines().getFirst().isFailed());
        assertEquals(expectedFailedReason, result.getLines().getFirst().getFailedReason());
    }

    private static SecurityTransaction getSecurityTransaction() {
        final SecurityTransaction transaction = new SecurityTransaction();
        transaction.setAccountId(DEFAULT_ACCOUNT.getId());
        transaction.setSecurityId(WOLTER_KLUWER.getId());
        transaction.setIsin("NL0000395903");
        transaction.setAmount(8);
        transaction.setPrice(146.5);
        transaction.setDescription("Koop 8 @ 146,5 EUR");
        transaction.setTimestamp(Instant.parse("2024-02-28T12:26:00Z"));
        transaction.setCash(432.18);
        transaction.setTransactionType(SecurityTransactionType.BUY);
        transaction.setCurrencyId(CURRENCY_EURO_ID);
        return transaction;
    }
}
