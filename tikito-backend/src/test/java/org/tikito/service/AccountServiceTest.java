package org.tikito.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateAccountRequest;
import org.tikito.dto.AccountDto;
import org.tikito.entity.money.MoneyHolding;

import java.util.List;

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
        final AccountDto dto = createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);

        final MoneyHolding moneyHolding = moneyHoldingRepository.findByUserIdAndAccountId(DEFAULT_USER_ACCOUNT.getId(), dto.getId()).getFirst();
        assertEquals(CURRENCY_EURO_ID, moneyHolding.getCurrencyId());
    }

    @Test
    void testCreateNewCreditAccount() {
        final AccountDto dto = createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);

        final MoneyHolding moneyHolding = moneyHoldingRepository.findByUserIdAndAccountId(DEFAULT_USER_ACCOUNT.getId(), dto.getId()).getFirst();
        assertEquals(CURRENCY_EURO_ID, moneyHolding.getCurrencyId());
    }

    @Test
    void shouldHaveSingleHolding_givenDeletedAccount() {
        final AccountDto deleted = createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);
        assertEquals(1, moneyHoldingRepository.count());
        accountService.deleteAccount(deleted.getUserId(), deleted.getId());

        final AccountDto securityAccount = createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);
        assertEquals(0, moneyHoldingRepository.count());
        accountService.deleteAccount(securityAccount.getUserId(), securityAccount.getId());

        final AccountDto newDto = createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);
        final List<MoneyHolding> all = moneyHoldingRepository.findAll();
        assertEquals(1, all.size());

        final MoneyHolding holding = all.getFirst();
        assertHoldingMatches(newDto, holding);
    }

    @Test
    void shouldSetProperHolding_given_updatedAccount() {
        final AccountDto account = createAccount(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE);
        final long holdingId = moneyHoldingRepository.findAll().getFirst().getId();

        accountService.createOrUpdate(account.getUserId(), request("new", account.getId()));
        final List<MoneyHolding> allHoldings = moneyHoldingRepository.findAll();
        assertEquals(1, allHoldings.size());
        assertEquals(holdingId, allHoldings.getFirst().getId().longValue());

        accountService.createOrUpdate(account.getUserId(), request("new-new", account.getId()));
        assertEquals(0, moneyHoldingRepository.count());
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