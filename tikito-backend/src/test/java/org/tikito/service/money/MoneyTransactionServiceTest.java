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
import java.time.LocalDate;
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

    // todo: add more test for this class
}