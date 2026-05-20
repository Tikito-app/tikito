package org.tikito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateAccountRequest;
import org.tikito.dto.AccountDto;
import org.tikito.entity.Account;
import org.tikito.entity.money.MoneyHolding;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@Transactional
class AccountServiceTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        withDefaultCurrencies();
        withDefaultUserAccount();
    }

    @Test
    void testCreateNewDebitAccount() {
        createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);

        final List<Account> accounts = accountRepository.findByUserIdAndName(DEFAULT_USER_ACCOUNT.getId(), Set.of(ACCOUNT_NAME_ONE));
        assertEquals(1, accounts.size());
    }

    private void assertHoldingMatches(final AccountDto account, final MoneyHolding holding) {
        assertEquals(account.getId().longValue(), holding.getAccountId());
        assertEquals(account.getCurrencyId(), holding.getCurrencyId());
        assertEquals(account.getUserId(), holding.getUserId());
    }

    private AccountDto createAccount(final long userId, final String accountName) {
        final AccountDto dto = accountService.createOrUpdate(userId, request(accountName));
        assertEquals(userId, dto.getUserId());
        assertEquals(accountName, dto.getName());
        assertEquals(CURRENCY_EURO_ID, dto.getCurrencyId());
        assertEquals(ACCOUNT_NUMBER_ONE, dto.getAccountNumber());
        return dto;
    }

    private CreateOrUpdateAccountRequest request(final String name) {
        return request(name, null);
    }

    private CreateOrUpdateAccountRequest request(final String name, final Long id) {
        final CreateOrUpdateAccountRequest request = new CreateOrUpdateAccountRequest();
        request.setId(id);
        request.setCurrencyId(CURRENCY_EURO_ID);
        request.setAccountNumber(ACCOUNT_NUMBER_ONE);
        request.setName(name);
        return request;
    }
}