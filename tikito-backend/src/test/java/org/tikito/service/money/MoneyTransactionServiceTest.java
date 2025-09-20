package org.tikito.service.money;

import org.tikito.dto.AccountDto;
import org.tikito.dto.money.MoneyTransactionImportLine;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.service.BaseIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class MoneyTransactionServiceTest extends BaseIntegrationTest {
    @Autowired
    private MoneyTransactionService service;
    private MoneyTransaction transaction;

    @BeforeEach
    void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
        loginWithDefaultUser();
        transaction = withDefaultMoneyTransactions(DEFAULT_ACCOUNT_DTO).getFirst();
        withDefaultMoneyTransactionGroups();
    }

    @Test
    void setTransactionGroupId_shouldSetGroupId_givenValidGroupId() {
        assertNull(transaction.getGroupId());
        service.setTransactionGroupId(DEFAULT_USER_ACCOUNT.getId(), transaction.getId(), TRANSACTION_GROUP_REGEX.getId());
        transaction = moneyTransactionRepository.findById(transaction.getId()).orElseThrow();
        assertEquals(TRANSACTION_GROUP_REGEX.getId(), transaction.getGroupId());

        service.setTransactionGroupId(DEFAULT_USER_ACCOUNT.getId(), transaction.getId(), null);
        transaction = moneyTransactionRepository.findById(transaction.getId()).orElseThrow();
        assertNull(transaction.getGroupId());
    }

    @Test
    void setTransactionGroupId_shouldThrowException_givenInvalidTransactionId() {
        assertNull(transaction.getGroupId());
        Assertions.assertThrows(NoSuchElementException.class, () ->
                service.setTransactionGroupId(DEFAULT_USER_ACCOUNT.getId(), 1234L, TRANSACTION_GROUP_REGEX.getId()));
    }

    @Test
    void setTransactionGroupId_shouldThrowException_givenInvalidGroupId() {
        assertNull(transaction.getGroupId());
        Assertions.assertThrows(NoSuchElementException.class, () ->
                service.setTransactionGroupId(DEFAULT_USER_ACCOUNT.getId(), transaction.getId(), 1234L));
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

    // todo: add more test for this class
}