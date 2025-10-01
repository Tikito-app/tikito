package org.tikito.service;

import org.tikito.dto.AccountDto;
import org.tikito.dto.AccountType;
import org.tikito.dto.DateRange;
import org.tikito.dto.loan.LoanType;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;
import org.tikito.dto.money.MoneyTransactionGroupType;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Account;
import org.tikito.entity.UserAccount;
import org.tikito.entity.loan.Loan;
import org.tikito.entity.loan.LoanPart;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.entity.security.SecurityPrice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.tikito.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BaseIntegrationTest extends BaseTest {

    protected static Account DEFAULT_ACCOUNT = null;
    protected static Account DOLLAR_ACCOUNT = null;
    protected static AccountDto DEFAULT_ACCOUNT_DTO = null;
    protected static AccountDto DOLLAR_ACCOUNT_DTO = null;
    protected static Security WOLTER_KLUWER = null;
    protected static Security ALPHABET = null;
    protected static Security AMAZON = null;
    protected static MoneyTransactionGroup TRANSACTION_GROUP_REGEX = null;
    protected static MoneyTransactionGroup TRANSACTION_GROUP_CLUSTER = null;


    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected LoanRepository loanRepository;

    @Autowired
    protected MoneyTransactionGroupRepository transactionGroupRepository;

    @Autowired
    protected IsinRepository isinRepository;

    @Autowired
    protected SecurityTransactionRepository securityTransactionRepository;

    @Autowired
    protected SecurityRepository securityRepository;

    @Autowired
    protected JobRepository jobRepository;

    @Autowired
    protected SecurityHoldingRepository securityHoldingRepository;

    @Autowired
    protected SecurityPriceRepository securityPriceRepository;

    @Autowired
    protected MoneyTransactionRepository moneyTransactionRepository;

    @Autowired
    protected HistoricalMoneyHoldingValueRepository historicalMoneyHoldingValueRepository;

    @Autowired
    protected HistoricalSecurityHoldingValueRepository historicalSecurityHoldingValueRepository;

    @Autowired
    protected AggregatedHistoricalMoneyHoldingValueRepository aggregatedHistoricalMoneyHoldingValueRepository;

    @Autowired
    protected CacheService cacheService;

    @Autowired
    protected UserAccountRepository userAccountRepository;

    @AfterEach
    @BeforeEach
    public void tearDown() {
        accountRepository.deleteAll();
        isinRepository.deleteAll();
        jobRepository.deleteAll();
        securityHoldingRepository.deleteAll();
        securityTransactionRepository.deleteAll();
        securityPriceRepository.deleteAll();
        securityRepository.deleteAll();
        moneyTransactionRepository.deleteAll();
        transactionGroupRepository.deleteAll();
        historicalMoneyHoldingValueRepository.deleteAll();
        userAccountRepository.deleteAll();
        loanRepository.deleteAll();
    }

    protected void withDefaultData() {
        withDefaultCurrencies();
        withDefaultSecurities();
        withDefaultAccounts();
    }

    protected void loginWithDefaultUser() {
        if (DEFAULT_USER_ACCOUNT == null) {
            DEFAULT_USER_ACCOUNT = withDefaultUserAccount();
        }
        loginWithUser(DEFAULT_USER_ACCOUNT.getId());
    }

    protected void withDefaultCurrencies() {
        CURRENCY_EURO_ID = withExistingCurrency("EUR", "Euro").getId();
        CURRENCY_DOLLAR_ID = withExistingCurrency("USD", "Dollar").getId();

        withExistingCurrencyCache(CURRENCY_DOLLAR_ID);

        cacheService.refreshCurrencies();
    }

    private void withExistingCurrencyCache(final long currencyId) {
        final LocalDate now = LocalDate.now();
        final List<SecurityPrice> prices = new ArrayList<>();
        for (LocalDate date = now.minusDays(365 * 10);
             date.isBefore(now.minusDays(1));
             date = date.plusDays(1)) {
            final SecurityPrice price = new SecurityPrice();
            price.setDate(date);
            price.setSecurityId(currencyId);
            price.setPrice(randomDouble(2, 3));
            prices.add(price);
        }
        securityPriceRepository.saveAllAndFlush(prices);
    }

    protected Security withExistingCurrency(final String identifier, final String displayName) {
        final Security security = new Security();
        security.setName(displayName);
        security.setSecurityType(SecurityType.CURRENCY);
        security.setCurrentIsin(identifier);
        final Security persistedSecurity = securityRepository.saveAndFlush(security);
        final Isin isin = new Isin(identifier);
        isin.setSecurityId(persistedSecurity.getId());
        isinRepository.saveAndFlush(isin);
        return persistedSecurity;
    }

    protected void withDefaultAccounts() {
        DEFAULT_ACCOUNT = withExistingAccounts(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_ONE, ACCOUNT_NUMBER_ONE, AccountType.SECURITY, CURRENCY_EURO_ID);
        DOLLAR_ACCOUNT = withExistingAccounts(DEFAULT_USER_ACCOUNT.getId(), ACCOUNT_NAME_TWO, ACCOUNT_NUMBER_TWO, AccountType.SECURITY, CURRENCY_DOLLAR_ID);
        DEFAULT_ACCOUNT_DTO = DEFAULT_ACCOUNT.toDto();
        DOLLAR_ACCOUNT_DTO = DOLLAR_ACCOUNT.toDto();
    }

    protected void withDefaultMoneyTransactionGroups() {
        TRANSACTION_GROUP_REGEX = withExistingTransactionGroup(
                DEFAULT_ACCOUNT.getId(),
                "My Regex Group",
                MoneyTransactionGroupQualifierType.REGEX, "AH ([0-9]+) (.*)",
                Set.of(MoneyTransactionGroupType.MONEY, MoneyTransactionGroupType.BUDGET, MoneyTransactionGroupType.LOAN));
        TRANSACTION_GROUP_CLUSTER = withExistingTransactionGroup(
                DEFAULT_ACCOUNT.getId(),
                "My Cluster Group",
                MoneyTransactionGroupQualifierType.SIMILAR, "AH 134 test",
                Set.of(MoneyTransactionGroupType.MONEY, MoneyTransactionGroupType.BUDGET, MoneyTransactionGroupType.LOAN));
    }

    protected MoneyTransactionGroup withExistingTransactionGroup(final Long accountId,
                                                                 final String name,
                                                                 final MoneyTransactionGroupQualifierType qualifierType,
                                                                 final String qualifier,
                                                                 final Set<MoneyTransactionGroupType> groupTypes) {
        final MoneyTransactionGroup group = new MoneyTransactionGroup();
        group.setName(name);
        group.setUserId(DEFAULT_USER_ACCOUNT.getId());
        group.setGroupTypes(groupTypes);
//        group.setQualifiers(new ArrayList<>(List.of(new MoneyTransactionGroupQualifier(group, qualifierType, qualifier, MoneyTransactionField.DESCRIPTION))));
        return transactionGroupRepository.saveAndFlush(group);
    }

    protected List<MoneyTransaction> withDefaultMoneyTransactions(final AccountDto account) {
        final double v1 = randomDouble(200, 300);
        final double v2 = randomDouble(50, 100);
        final double v3 = randomDouble(-150, -100);
        return List.of(
                withExistingMoneyTransaction(DEFAULT_USER_ACCOUNT.getId(), account.getId(), NOW_TIME.minus(35, ChronoUnit.DAYS), account.getCurrencyId(), v1, v1, COUNTERPART_ACCOUNT_NUMBER, COUNTERPART_ACCOUNT_NAME),
                withExistingMoneyTransaction(DEFAULT_USER_ACCOUNT.getId(), account.getId(), NOW_TIME.minus(15, ChronoUnit.DAYS), account.getCurrencyId(), v2, v1 + v2, COUNTERPART_ACCOUNT_NUMBER, COUNTERPART_ACCOUNT_NAME),
                withExistingMoneyTransaction(DEFAULT_USER_ACCOUNT.getId(), account.getId(), NOW_TIME, account.getCurrencyId(), v3, v1 + v2 + v3, COUNTERPART_ACCOUNT_NUMBER, COUNTERPART_ACCOUNT_NAME));
    }

    protected MoneyTransaction withExistingMortgageTransaction(final long loanId, final long groupId, final LocalDate date, final double amount) {
        final MoneyTransaction transaction = moneyTransactionRepository.saveAndFlush(moneyTransaction(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId(), date.atStartOfDay().plusHours(5).toInstant(ZoneOffset.UTC), CURRENCY_EURO_ID, amount, 0, "", "", "Mortgage"));
        transaction.setGroupId(groupId);
        transaction.setLoanId(loanId);
        return moneyTransactionRepository.saveAndFlush(transaction);
    }


    protected MoneyTransaction withExistingMoneyTransaction(final long userId,
                                                           final long accountId,
                                                           final Instant timestamp,
                                                           final long currencyId,
                                                           final double amount,
                                                           final double finalBalance,
                                                           final String counterpartAccountNumber,
                                                           final String counterpartAccountName) {
        return moneyTransactionRepository.saveAndFlush(moneyTransaction(userId, accountId, timestamp, currencyId, amount, finalBalance, counterpartAccountNumber, counterpartAccountName, ""));
    }

    protected Loan withExistingLoan() {
        final String name = "Mortgage";
        final LocalDate firstPartStartDate = LocalDate.of(2025, 4, 15);
        final LocalDate secondPartStartDate = LocalDate.of(2025, 4, 20);
        final LocalDate endDate = LocalDate.of(2026, 4, 30);
        return withExistingLoan(DateRange.MONTH, name, new ArrayList<>(List.of(
                loanPart(name + " - part 1", 400, 0, firstPartStartDate, endDate, LoanType.MORTGAGE_ANNUITEIT,
                        new ArrayList<>(List.of(loanInterest(3, firstPartStartDate, endDate.minusMonths(4)),
                                loanInterest(3, endDate.minusMonths(4), endDate)))),
                loanPart(name + " - part 2", 600, 0, secondPartStartDate, endDate, LoanType.MORTGAGE_ANNUITEIT,
                        new ArrayList<>(List.of(loanInterest(5, secondPartStartDate, endDate))))
        )));
    }

    protected Loan withExistingLoan(final DateRange dateRange, final String name, final List<LoanPart> loanParts) {
        final Loan loan = new Loan(DEFAULT_USER_ACCOUNT.getId());
        loan.setDateRange(dateRange);
        loan.setName(name);
        loan.setLoanParts(loanParts);
        loanParts.forEach(loanPart -> {
            loanPart.setLoan(loan);
            loanPart.getInterests().forEach(interest -> interest.setLoanPart(loanPart));
        });
        return loanRepository.saveAndFlush(loan);
    }

    protected Account withExistingAccounts(final long userId, final String name, final String accountNumber, final AccountType accountType, final long currencyId) {
        final Account account = new Account();
        account.setName(name);
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountType);
        account.setCurrencyId(currencyId);
        account.setUserId(userId);
        return accountRepository.saveAndFlush(account);
    }

    protected void withDefaultSecurities() {
        final LocalDate toDate = ONE_YEAR_AGO.minusDays(5);
        WOLTER_KLUWER = withExistingSecurity("WOLTERS KLUWER", CURRENCY_EURO_ID, new ArrayList<>(List.of(
                isin(ISIN_ONE_OLD, TWENTY_YEARS_AGO, toDate, "WKL.AS"),
                isin(ISIN_ONE, toDate, "WKL.AS"))));
    }

    protected Security withExistingSecurity(final String name, final long currencyId, final List<Isin> isins) {
        final Security security = securityRepository.saveAndFlush(security(name, currencyId, isins.getLast().getIsin()));
        isins.forEach(isin -> isin.setSecurityId(security.getId()));
        isinRepository.saveAllAndFlush(isins);
        return security;
    }

    protected UserAccount withDefaultUserAccount() {
        DEFAULT_USER_ACCOUNT = withExistingUserAccount(randomString(10), DEFAULT_USER_ACCOUNT_PASSWORD, null);
        return DEFAULT_USER_ACCOUNT;
    }

    protected UserAccount withExistingUserAccount(final String email, final String password, final String activationCode) {
        final UserAccount userAccount = userAccountRepository.saveAndFlush(userAccount(email, password, activationCode));
        cacheService.refreshFirstEverUser();
        return userAccount;
    }
}
