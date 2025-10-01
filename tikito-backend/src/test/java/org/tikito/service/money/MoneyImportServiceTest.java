package org.tikito.service.money;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.AccountDto;
import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.dto.money.MoneyTransactionImportResultDto;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.exception.CannotReadFileException;
import org.tikito.repository.MoneyTransactionRepository;
import org.tikito.service.BaseIntegrationTest;
import org.tikito.util.MoneyImportLineBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tikito.dto.security.SecurityTransactionImportResultDto.FAILED_DUPLICATE_TRANSACTION;
import static org.tikito.service.importer.money.CustomMoneyImportHeaderName.*;

@SpringBootTest
@Transactional
class MoneyImportServiceTest extends BaseIntegrationTest {

    @Autowired
    private MoneyTransactionRepository repository;

    @Autowired
    private MoneyImportService service;

    @BeforeEach
    public void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
    }

    private void defaultImportCsv(final MoneyImportLineBuilder builder) {
        final String csv = builder.getTimestamp() + ";" +
                builder.getCounterpartAccountName() + ";" +
                "my-number;" +
                builder.getCounterpartAccountNumber() + ";" +
                "GT;" +
                builder.getDebitCredit() + ";" +
                builder.getAmount() + ";" +
                builder.getTransactionType() + ";" +
                "notifications;" +
                builder.getFinalBalance() + ";" +
                builder.getCounterpartAccountName() + ";" +
                builder.getCounterpartAccountName() + ";";
        try {
            service.importTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId(), new MockMultipartFile("Abn.csv", csv.getBytes()), false,
                    Map.of(), "debit", "dd-MM-yyyy", "dd-MM-yyyy", null, ";");
        } catch (final CannotReadFileException e) {
            Assertions.fail();
        }
    }

    @Test
    void testImportTransactionBasicInfo() throws IOException {
        final MockMultipartFile file = getClassPathResourceToImport("bank-transaction/abn.csv", "Abn.csv");

        final MoneyTransactionImportResultDto result = service.importTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId(), file, false,
                Map.of(), "debit", "dd-MM-yyyy", "dd-MM-yyyy", null, ";");

        validateResults(result);
    }

    @Test
    void testImportTransactionDuplicate() throws IOException {
        final MoneyTransaction transaction = getMoneyTransaction();
        moneyTransactionRepository.save(transaction);
        final MockMultipartFile file = getClassPathResourceToImport("bank-transaction/abn-duplicate.csv", "Abn.csv");

        final MoneyTransactionImportResultDto result = service.importTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId(), file, false,
                Map.of(), "debit", "dd-MM-yyyy", "dd-MM-yyyy", null, ";");
        assertEquals(4, result.getLines().size());
        assertEquals(3, result.getImportedTransactions().size());
        assertEquals(1, result.getLines().stream().filter(line -> FAILED_DUPLICATE_TRANSACTION.equals(line.getFailedReason())).count());

        final MoneyTransactionImportResultDto newResult = service.importTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId(), file, false,
                Map.of(), "debit", "dd-MM-yyyy", "dd-MM-yyyy", null, ";");
        assertEquals(4, newResult.getLines().size());
        assertEquals(0, newResult.getImportedTransactions().size());
    }

    @Test
    void testImportCustomHeaders() throws IOException {
        final MockMultipartFile file = getClassPathResourceToImport("bank-transaction/custom-headers.csv", "Abn.csv");

        final MoneyTransactionImportResultDto result = service.importTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId(), file, false,
                Map.of(CURRENCY, 1,
                        TIMESTAMP, 2,
                        FINAL_BALANCE, 5,
                        AMOUNT, 6,
                        DESCRIPTION, 7), "debit", "yyyyMMdd", "yyyyMMdd", null, ";");

        validateResults(result);
    }


    @Test
    void enrichValidateAndMap_shouldAssignDifferentCurrency_givenDifferentAccountCurrency() {
        final AccountDto accountDto = new AccountDto();
        final MoneyTransactionImportLine line = new MoneyTransactionImportLine(0, List.of());
        final List<MoneyTransactionImportLine> lines = new ArrayList<>();
        accountDto.setCurrencyId(CURRENCY_EURO_ID);
        line.setCurrency("USD");
        line.setTimestamp(Instant.now());
        lines.add(line);
        service.enrichValidateAndMap(accountDto, lines, 0);
        assertEquals(CURRENCY_DOLLAR_ID, line.getCurrencyId());
    }

    @Test
    void enrichValidateAndMap_shouldAssignCurrency_givenNoCurrencyOnLine() {
        final AccountDto accountDto = new AccountDto();
        final MoneyTransactionImportLine line = new MoneyTransactionImportLine(0, List.of());
        final List<MoneyTransactionImportLine> lines = new ArrayList<>();
        accountDto.setCurrencyId(CURRENCY_EURO_ID);
        line.setCurrency(null);
        line.setTimestamp(Instant.now());
        lines.add(line);
        service.enrichValidateAndMap(accountDto, lines, 0);
        assertEquals(CURRENCY_EURO_ID, line.getCurrencyId());
    }

    @Test
    void enrichValidateAndMap_removeSpacesOfAccountNumber() {
        final AccountDto accountDto = new AccountDto();
        final MoneyTransactionImportLine line = new MoneyTransactionImportLine(0, List.of());
        final List<MoneyTransactionImportLine> lines = new ArrayList<>();
        line.setCounterpartAccountNumber(" 1234    657 ");
        line.setTimestamp(Instant.now());
        lines.add(line);
        service.enrichValidateAndMap(accountDto, lines, 0);
        assertEquals("1234657", line.getCounterpartAccountNumber());
    }

    void validateResults(final MoneyTransactionImportResultDto result) {

        final Instant expectedTimestamp1 = LocalDateTime.of(2024, 11, 12, 0, 0).toInstant(ZoneOffset.UTC);
        assertEquals(3, result.getLines().size());
        assertEquals(3, result.getImportedTransactions().size());
        final MoneyTransaction transaction1 = result.getImportedTransactions().get(0);
        final MoneyTransaction transaction2 = result.getImportedTransactions().get(1);
        final MoneyTransaction transaction3 = result.getImportedTransactions().get(2);

        assertEquals(DEFAULT_ACCOUNT.getId(), transaction1.getAccountId());
        assertEquals(expectedTimestamp1, transaction1.getTimestamp());
        assertEquals(CURRENCY_EURO_ID, transaction1.getCurrencyId());
        assertEquals(250, transaction1.getAmount());
        assertEquals(250, transaction1.getFinalBalance());
        assertEquals("NL13INGB0001234567", transaction1.getCounterpartAccountNumber());


        final Instant expectedTimestamp2 = LocalDateTime.of(2024, 11, 17, 10, 42).toInstant(ZoneOffset.UTC);
        assertEquals(expectedTimestamp2, transaction2.getTimestamp());
        assertEquals(221.75, transaction2.getFinalBalance());
        assertEquals(-28.25, transaction2.getAmount());

        final Instant expectedTimestamp3 = LocalDateTime.of(2024, 11, 19, 11, 43).toInstant(ZoneOffset.UTC);
        assertEquals(expectedTimestamp3, transaction3.getTimestamp());

        final List<MoneyTransaction> allInDb = repository.findAll().stream().sorted(Comparator.comparing(MoneyTransaction::getTimestamp)).toList();
        assertEquals(3, allInDb.size());
        final MoneyTransaction transaction = allInDb.getFirst();

        assertEquals(DEFAULT_ACCOUNT.getId(), transaction.getAccountId());
        assertEquals(expectedTimestamp1, transaction.getTimestamp());
        assertEquals(CURRENCY_EURO_ID, transaction.getCurrencyId());
        assertEquals(250, transaction.getAmount());
        assertEquals(250, transaction.getFinalBalance());
        assertEquals("NL13INGB0001234567", transaction.getCounterpartAccountNumber());
        assertEquals("SEPA Overboeking                 IBAN: NL13INGB0001234567        BIC: INGBNL2A                    Naam: Hr Test Person              ", transaction.getDescription());
    }

    private static MoneyTransaction getMoneyTransaction() {
        final MoneyTransaction transaction = new MoneyTransaction();
        transaction.setAccountId(DEFAULT_ACCOUNT.getId());
        transaction.setTimestamp(Instant.parse("2023-11-19T11:43:00Z"));
        transaction.setCurrencyId(CURRENCY_EURO_ID);
        transaction.setAmount(-3.14);
        transaction.setUserId(DEFAULT_USER_ACCOUNT.getId());
        transaction.setFinalBalance(218.61);
        transaction.setDescription("BEA, Betaalpas                   ALBERT HEIJN 1234,PAS196        NR:NQ9NRN, 19.11.24/11:43        Amsterdam");
        return transaction;
    }
}