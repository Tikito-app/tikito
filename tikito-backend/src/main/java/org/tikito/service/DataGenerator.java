package org.tikito.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateLoanPartRequest;
import org.tikito.controller.request.CreateOrUpdateLoanRequest;
import org.tikito.controller.request.CreateOrUpdateMoneyTransactionGroupRequest;
import org.tikito.dto.DateRange;
import org.tikito.dto.loan.LoanInterestDto;
import org.tikito.dto.loan.LoanType;
import org.tikito.dto.loan.MortgageCalculator;
import org.tikito.dto.money.MoneyTransactionField;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;
import org.tikito.dto.money.MoneyTransactionGroupType;
import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Account;
import org.tikito.entity.money.MoneyHolding;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.security.*;
import org.tikito.exception.EmailAlreadyExistsException;
import org.tikito.exception.PasswordNotLongEnoughException;
import org.tikito.repository.*;
import org.tikito.service.money.MoneyHoldingService;
import org.tikito.service.money.MoneyTransactionGroupService;
import org.tikito.service.security.SecurityHoldingService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Generates a full set of realistic-looking fake data (accounts, securities, transactions, budgets, a mortgage) for
 * a freshly registered "demo" user. Intended for taking screenshots of the application; all security prices are
 * synthesised locally instead of being fetched from a real market data provider, so this runs offline and fast.
 */
@Service
@Profile("data-generator")
@Transactional
@Slf4j
public class DataGenerator {
    private static final long USER_ID = 1;
    private static long EURO_ID = 1;

    private final SecurityTransactionRepository securityTransactionRepository;
    private final MoneyTransactionGroupService moneyTransactionGroupService;
    private final LocalDate NOW = LocalDate.now();
    private final LocalDate FIVE_YEARS_AGO = NOW.minusDays(365 * 5);
    private final AccountRepository accountRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;
    private final SecurityHoldingService securityHoldingService;
    private final SecurityRepository securityRepository;
    private final IsinRepository isinRepository;
    private final SecurityPriceRepository securityPriceRepository;
    private final MoneyHoldingService moneyHoldingService;
    private final MoneyHoldingRepository moneyHoldingRepository;
    private final UserAccountService userAccountService;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final CacheService cacheService;
    private final LoanService loanService;
    private final LoanValueService loanValueService;
    private final EntityManager entityManager;

    private Account SAVINGS_ACCOUNT;
    private Account DEBIT_ACCOUNT;
    private Account STOCK_ACCOUNT;

    private final Map<Long, Map<LocalDate, SecurityPrice>> pricesPerDateAndSecurityId = new HashMap<>();

    public DataGenerator(final SecurityTransactionRepository securityTransactionRepository,
                         final MoneyTransactionGroupService moneyTransactionGroupService,
                         final AccountRepository accountRepository,
                         final UserAccountService userAccountService,
                         final MoneyTransactionRepository moneyTransactionRepository,
                         final SecurityHoldingService securityHoldingService,
                         final SecurityRepository securityRepository,
                         final IsinRepository isinRepository,
                         final SecurityPriceRepository securityPriceRepository,
                         final MoneyHoldingService moneyHoldingService,
                         final MoneyHoldingRepository moneyHoldingRepository,
                         final SecurityHoldingRepository securityHoldingRepository,
                         final CacheService cacheService,
                         final LoanService loanService,
                         final LoanValueService loanValueService,
                         final EntityManager entityManager) {
        this.securityTransactionRepository = securityTransactionRepository;
        this.moneyTransactionGroupService = moneyTransactionGroupService;

        this.accountRepository = accountRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.securityHoldingService = securityHoldingService;
        this.securityRepository = securityRepository;
        this.isinRepository = isinRepository;
        this.securityPriceRepository = securityPriceRepository;
        this.moneyHoldingService = moneyHoldingService;
        this.moneyHoldingRepository = moneyHoldingRepository;

        this.userAccountService = userAccountService;
        this.securityHoldingRepository = securityHoldingRepository;

        this.cacheService = cacheService;
        this.loanService = loanService;
        this.loanValueService = loanValueService;
        this.entityManager = entityManager;
    }

    public void generate() throws PasswordNotLongEnoughException, EmailAlreadyExistsException {
        userAccountService.register("demo", "demodemodemo");
        generateAccounts();
        EURO_ID = securityRepository.findAll().stream()
                .filter(security -> security.getName().equals("Euro"))
                .findFirst()
                .get()
                .getId();

        final List<Security> securities = generateSecurities();
        securities.forEach(this::generateSecurityPricesFor);
        securities.forEach(security -> {
            createSecurityHoldings(security, STOCK_ACCOUNT.getId());
            generateSecurityTransactions(security, STOCK_ACCOUNT.getId());
        });
        securities.forEach(security -> securityHoldingService.recalculateHistoricalValue(USER_ID, security.getId()));
        securityHoldingService.recalculateAggregatedHistoricalHoldingValues(USER_ID);

        generateMoneyHoldings();
        final long mortgageGroupId = generateMoneyTransactionGroups();
        final LoanSetup loanSetup = generateLoan(mortgageGroupId);
        generateMoneyTransactions(loanSetup.monthlyPayment());

        moneyTransactionGroupService.groupTransactions(USER_ID);

        moneyHoldingService.recalculateHistoricalHoldingValues(USER_ID, DEBIT_ACCOUNT.getId());
        moneyHoldingService.recalculateHistoricalHoldingValues(USER_ID, SAVINGS_ACCOUNT.getId());
        moneyHoldingService.recalculateAggregatedHistoricalHoldingValues(USER_ID);

        moneyTransactionGroupService.recalculateHistoricalBudget(USER_ID);
        loanValueService.generateLoanValues(USER_ID, loanSetup.loanId());

        cacheService.refreshCurrencies();
        cacheService.refreshSecurities();
    }

    public void generateAccounts() {
        SAVINGS_ACCOUNT = new Account();
        SAVINGS_ACCOUNT.setAccountNumber("NL91TIKI0000001");
        SAVINGS_ACCOUNT.setUserId(USER_ID);
        SAVINGS_ACCOUNT.setName("Savings Account");
        SAVINGS_ACCOUNT.setCurrencyId(EURO_ID);
        accountRepository.saveAndFlush(SAVINGS_ACCOUNT);

        DEBIT_ACCOUNT = new Account();
        DEBIT_ACCOUNT.setAccountNumber("NL91TIKI0000002");
        DEBIT_ACCOUNT.setUserId(USER_ID);
        DEBIT_ACCOUNT.setName("Checking Account");
        DEBIT_ACCOUNT.setCurrencyId(EURO_ID);
        accountRepository.saveAndFlush(DEBIT_ACCOUNT);

        STOCK_ACCOUNT = new Account();
        STOCK_ACCOUNT.setAccountNumber("NL91TIKI0000003");
        STOCK_ACCOUNT.setUserId(USER_ID);
        STOCK_ACCOUNT.setName("Investment Account");
        STOCK_ACCOUNT.setCurrencyId(EURO_ID);
        accountRepository.saveAndFlush(STOCK_ACCOUNT);
    }

    public List<Security> generateSecurities() {
        return new ArrayList<>(List.of(
                createSecurity("Nova Robotics N.V.", SecurityType.STOCK, "Technology", "Robotics", "AMS", "NL0000000001", "NOVA.AS"),
                createSecurity("Global Growth Index Fund", SecurityType.ETF, "Diversified", "Index Fund", "AMS", "IE0000000002", "GGIF.AS"),
                createSecurity("Ethermoon", SecurityType.CRYPTO, "Crypto", "Currency", "CRYPTO", "XX0000000003", "ETHM-EUR")));
    }

    private Security createSecurity(final String name, final SecurityType type, final String sector, final String industry,
                                    final String exchange, final String isin, final String symbol) {
        Security security = new Security();
        security.setName(name);
        security.setCurrencyId(EURO_ID);
        security.setSecurityType(type);
        security.setSector(sector);
        security.setIndustry(industry);
        security.setExchange(exchange);
        security.setCurrentIsin(isin);
        security = securityRepository.saveAndFlush(security);

        final Isin isinEntity = new Isin();
        isinEntity.setIsin(isin);
        isinEntity.setSymbol(symbol);
        isinEntity.setSecurityId(security.getId());
        isinEntity.setValidFrom(FIVE_YEARS_AGO.minusYears(5));
        isinRepository.saveAndFlush(isinEntity);

        return security;
    }

    /**
     * Picks a realistic price profile (starting price, average daily drift and daily volatility) per security type.
     */
    private void generateSecurityPricesFor(final Security security) {
        switch (security.getSecurityType()) {
            case STOCK -> generateSecurityPrices(security, 45, 0.00040, 0.020);
            case ETF -> generateSecurityPrices(security, 60, 0.00025, 0.009);
            case CRYPTO -> generateSecurityPrices(security, 35, 0.00070, 0.045);
            default ->
                    throw new IllegalStateException("No demo price profile for security type " + security.getSecurityType());
        }
    }

    /**
     * Generates a daily price series for the last 5 years using a geometric random walk, so prices stay positive and
     * volatility naturally scales with the price.
     */
    public void generateSecurityPrices(final Security security, final double startPrice, final double dailyDrift, final double dailyVolatility) {
        final List<SecurityPrice> prices = new ArrayList<>();
        LocalDate currentDate = FIVE_YEARS_AGO;
        double lastPrice = startPrice;

        pricesPerDateAndSecurityId.put(security.getId(), new HashMap<>());

        while (currentDate.isBefore(NOW)) {
            final double dailyReturn = dailyDrift + randomDouble(-dailyVolatility, dailyVolatility);
            lastPrice = Math.max(0.01, lastPrice * (1 + dailyReturn));

            final SecurityPrice price = new SecurityPrice();
            price.setSecurityId(security.getId());
            price.setPrice(lastPrice);
            price.setDate(currentDate);

            pricesPerDateAndSecurityId.get(security.getId()).put(currentDate, price);
            prices.add(price);
            currentDate = currentDate.plusDays(1);
        }

        securityPriceRepository.saveAllAndFlush(prices);
        securityRepository.setLastPriceDate(security.getId(), prices.getLast().getDate());
    }

    /**
     * A security holding is stored twice: once scoped to the account it lives in, and once aggregated across all
     * accounts (accountId == null). Both are recalculated from the transactions and prices afterward.
     */
    private void createSecurityHoldings(final Security security, final long accountId) {
        final SecurityHolding accountHolding = new SecurityHolding();
        accountHolding.setUserId(USER_ID);
        accountHolding.setAccountId(accountId);
        accountHolding.setSecurityId(security.getId());
        accountHolding.setSecurityType(security.getSecurityType());
        accountHolding.setCurrencyId(EURO_ID);
        securityHoldingRepository.saveAndFlush(accountHolding);

        final SecurityHolding aggregatedHolding = new SecurityHolding();
        aggregatedHolding.setUserId(USER_ID);
        aggregatedHolding.setAccountId(null);
        aggregatedHolding.setSecurityId(security.getId());
        aggregatedHolding.setSecurityType(security.getSecurityType());
        aggregatedHolding.setCurrencyId(EURO_ID);
        securityHoldingRepository.saveAndFlush(aggregatedHolding);
    }

    public void generateSecurityTransactions(final Security security, final long accountId) {
        LocalDate currentDate = FIVE_YEARS_AGO;
        int totalAmount = 0;
        final List<SecurityTransaction> transactions = new ArrayList<>();

        while (currentDate.isBefore(NOW)) {
            final SecurityTransaction transaction = new SecurityTransaction();
            transaction.setTimestamp(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            transaction.setAccountId(accountId);
            transaction.setUserId(USER_ID);
            transaction.setSecurityId(security.getId());
            transaction.setCurrencyId(EURO_ID);
            transaction.setExchangeRate(1);

            int amount = randomInt(-15, 40);
            if (totalAmount + amount < 0) {
                amount = -totalAmount;
            }

            // by convention the price is stored negative for a BUY (money flowing out) and positive for a SELL
            transaction.setPrice(pricesPerDateAndSecurityId.get(security.getId()).get(currentDate).getPrice() * (amount < 0 ? 1 : -1));
            totalAmount += amount;
            transaction.setTransactionType(amount < 0 ? SecurityTransactionType.SELL : SecurityTransactionType.BUY);
            transaction.setAmount(Math.abs(amount));

            if (transaction.getAmount() > 0) {
                transactions.add(transaction);
            }
            currentDate = currentDate.plusDays(randomInt(60, 150));
        }

        if (security.getSecurityType() == SecurityType.STOCK) {
            transactions.addAll(generateDividends(security, accountId));
        }

        securityTransactionRepository.saveAllAndFlush(transactions);
    }

    private List<SecurityTransaction> generateDividends(final Security security, final long accountId) {
        final List<SecurityTransaction> dividends = new ArrayList<>();
        for (int year = 1; year <= 4; year++) {
            final SecurityTransaction dividend = new SecurityTransaction();
            dividend.setTimestamp(FIVE_YEARS_AGO.plusYears(year).atStartOfDay().toInstant(ZoneOffset.UTC));
            dividend.setAccountId(accountId);
            dividend.setUserId(USER_ID);
            dividend.setSecurityId(security.getId());
            dividend.setCurrencyId(EURO_ID);
            dividend.setExchangeRate(1);
            dividend.setTransactionType(SecurityTransactionType.DIVIDEND);
            dividend.setAmount(0);
            dividend.setPrice(randomDouble(20, 90));
            dividends.add(dividend);
        }
        return dividends;
    }

    /**
     * A money holding tracks the running balance per account/currency. It has to exist before the balance history
     * can be (re)calculated.
     */
    public void generateMoneyHoldings() {
        final List<MoneyHolding> holdings = new ArrayList<>();
        for (final Account account : List.of(DEBIT_ACCOUNT, SAVINGS_ACCOUNT)) {
            final MoneyHolding holding = new MoneyHolding();
            holding.setUserId(USER_ID);
            holding.setAccountId(account.getId());
            holding.setCurrencyId(EURO_ID);
            holdings.add(holding);
        }
        moneyHoldingRepository.saveAllAndFlush(holdings);
    }

    /**
     * Creates a handful of budget groups, plus a "Mortgage" group used to link the mortgage payments to the loan.
     * Returns the id of the mortgage group.
     */
    public long generateMoneyTransactionGroups() {
        createBudgetGroup("Groceries", "Supermarket", Set.of(DEBIT_ACCOUNT.getId()), 350d);
        createBudgetGroup("Utilities", "Energy Company", Set.of(DEBIT_ACCOUNT.getId()), 150d);
        createBudgetGroup("Entertainment", "Streaming Service,Cinema", Set.of(DEBIT_ACCOUNT.getId()), 80d);
        createBudgetGroup("Dining out", "Restaurant", Set.of(DEBIT_ACCOUNT.getId()), 200d);
        createBudgetGroup("Transport", "Public Transport", Set.of(DEBIT_ACCOUNT.getId()), 100d);
        createBudgetGroup("Shopping", "Shop", Set.of(DEBIT_ACCOUNT.getId()), 250d);
        createBudgetGroup("Salary", "Employer", Set.of(DEBIT_ACCOUNT.getId()), null);

        final CreateOrUpdateMoneyTransactionGroupRequest mortgageRequest = new CreateOrUpdateMoneyTransactionGroupRequest();
        mortgageRequest.setName("Mortgage");
        mortgageRequest.setGroupTypes(Set.of(MoneyTransactionGroupType.MONEY, MoneyTransactionGroupType.LOAN));
        mortgageRequest.setAccountIds(Set.of(DEBIT_ACCOUNT.getId()));
        mortgageRequest.setQualifiers(new ArrayList<>(List.of(
                new MoneyTransactionGroupQualifierDto(null, null, MoneyTransactionGroupQualifierType.INCLUDES, "Mortgage", MoneyTransactionField.COUNTERPARTY_NAME))));
        return moneyTransactionGroupService.createOrUpdateGroup(USER_ID, mortgageRequest).getId();
    }

    private void createBudgetGroup(final String name, final String counterpartyQualifier, final Set<Long> accountIds, final Double budgeted) {
        final CreateOrUpdateMoneyTransactionGroupRequest request = new CreateOrUpdateMoneyTransactionGroupRequest();
        request.setName(name);
        request.setGroupTypes(Set.of(MoneyTransactionGroupType.MONEY));
        request.setAccountIds(accountIds);
        request.setQualifiers(new ArrayList<>(List.of(
                new MoneyTransactionGroupQualifierDto(null, null, MoneyTransactionGroupQualifierType.INCLUDES, counterpartyQualifier, MoneyTransactionField.COUNTERPARTY_NAME))));
        if (budgeted != null) {
            request.setBudgeted(budgeted);
            request.setDateRange(DateRange.MONTH);
            request.setDateRangeAmount(-1);
            request.setStartDate(FIVE_YEARS_AGO);
        }
        moneyTransactionGroupService.createOrUpdateGroup(USER_ID, request);
    }

    /**
     * Creates a 30 year mortgage on the checking account, linked to the "Mortgage" group so the loan module has
     * data to show too. Returns the loan id and the monthly annuity payment, so the money transactions can mirror it.
     */
    private LoanSetup generateLoan(final long mortgageGroupId) {
        final double loanAmount = 300_000d;
        final double interestRatePercentPerYear = 3.8;
        final LocalDate loanStart = FIVE_YEARS_AGO;
        final LocalDate loanEnd = loanStart.plusYears(30);

        final CreateOrUpdateLoanRequest loanRequest = new CreateOrUpdateLoanRequest();
        loanRequest.setName("Home mortgage");
        loanRequest.setDateRange(DateRange.MONTH);
        final long loanId = loanService.createOrUpdateLoan(USER_ID, loanRequest).getId();

        // groups can only be linked to a loan that already has an id, so this is a second, separate update
        final CreateOrUpdateLoanRequest linkGroupRequest = new CreateOrUpdateLoanRequest();
        linkGroupRequest.setId(loanId);
        linkGroupRequest.setName("Home mortgage");
        linkGroupRequest.setDateRange(DateRange.MONTH);
        linkGroupRequest.setGroupIds(Set.of(mortgageGroupId));
        loanService.createOrUpdateLoan(USER_ID, linkGroupRequest);

        final CreateOrUpdateLoanPartRequest partRequest = new CreateOrUpdateLoanPartRequest();
        partRequest.setLoanId(loanId);
        partRequest.setName("Mortgage part 1");
        partRequest.setStartDate(loanStart);
        partRequest.setEndDate(loanEnd);
        partRequest.setAmount(loanAmount);
        partRequest.setLoanType(LoanType.MORTGAGE_ANNUITEIT);
        partRequest.setCurrencyId(EURO_ID);
        partRequest.setInterests(new ArrayList<>(List.of(
                new LoanInterestDto(null, loanStart, loanEnd, interestRatePercentPerYear))));
        loanService.createOrUpdateLoanPart(USER_ID, partRequest);

        // the Loan entity is still cached in this transaction's persistence context with a stale (empty)
        // loanParts collection, since createOrUpdateLoanPart never adds the new part to it in memory; clear the
        // context so LoanValueService reloads the loan (with its part) fresh from the database
        entityManager.clear();

        final long totalMonths = ChronoUnit.MONTHS.between(loanStart, loanEnd);
        final double monthlyPayment = MortgageCalculator.calculateAnnuiteit(loanAmount, interestRatePercentPerYear / 100 / 12, totalMonths);

        return new LoanSetup(loanId, monthlyPayment);
    }

    private record LoanSetup(long loanId, double monthlyPayment) {
    }

    public void generateMoneyTransactions(final double mortgagePayment) {
        final List<MoneyTransaction> transactions = new ArrayList<>();

        LocalDate date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Salary payment", "Employer BV", "NL13BANK0005365", randomDouble(2600, 3800), date));
            date = date.plusMonths(1);
        }

        // a yearly bonus, so income isn't perfectly flat every month
        date = FIVE_YEARS_AGO.plusMonths(randomInt(0, 12));
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Annual bonus", "Employer BV", "NL13BANK0005365", randomDouble(800, 4500), date));
            date = date.plusYears(1);
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Mortgage payment", "Mortgage", "NL13BANK0009988", -mortgagePayment, date));
            date = date.plusMonths(1);
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Utilities", "Energy Company", "NL13BANK0001111", randomDouble(-190, -60), date));
            date = date.plusMonths(1);
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Groceries", "Supermarket", "NL13BANK0001234", randomDouble(-170, -20), date));
            date = date.plusDays(randomInt(3, 11));
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Dining out", "Restaurant " + randomInt(1, 12), "NL13BANK0002222", randomDouble(-95, -12), date));
            date = date.plusDays(randomInt(2, 22));
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Transport", "Public Transport", "NL13BANK0003333", randomDouble(-100, -15), date));
            date = date.plusDays(randomInt(4, 16));
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            final String counterparty = randomInt(0, 2) == 0 ? "Streaming Service" : "Cinema";
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Entertainment", counterparty, "NL13BANK0004444", randomDouble(-75, -8), date));
            date = date.plusDays(randomInt(10, 50));
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Shop purchase", "Shop " + randomInt(1, 10), "NL13BANK0005555", randomDouble(-260, 80), date));
            date = date.plusDays(randomInt(1, 9));
        }

        date = FIVE_YEARS_AGO;
        while (date.isBefore(NOW)) {
            final double transferAmount = randomDouble(100, 600);
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), "Transfer to savings", "Savings Account", SAVINGS_ACCOUNT.getAccountNumber(), -transferAmount, date));
            transactions.add(newMoneyTransaction(SAVINGS_ACCOUNT.getId(), "Transfer from checking", "Checking Account", DEBIT_ACCOUNT.getAccountNumber(), transferAmount, date));
            date = date.plusMonths(1);
        }

        transactions.addAll(generateIrregularTransactions());

        moneyTransactionRepository.saveAllAndFlush(transactions);
    }

    /**
     * Occasional one-off expenses and windfalls scattered at random dates, so the balance history has real bumps in
     * it instead of a smooth, repetitive sawtooth.
     */
    private List<MoneyTransaction> generateIrregularTransactions() {
        final List<MoneyTransaction> transactions = new ArrayList<>();

        final List<String> expenseDescriptions = List.of("Car repair", "Medical expenses", "Home repair", "New electronics", "Holiday trip", "Gift given", "Vet bill", "Furniture");
        for (int i = 0; i < randomInt(10, 20); i++) {
            final String description = expenseDescriptions.get(randomInt(0, expenseDescriptions.size()));
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), description, description, "NL13BANK0006" + randomInt(100, 999),
                    -randomDouble(60, 1900), randomDateBetween(FIVE_YEARS_AGO, NOW)));
        }

        final List<String> incomeDescriptions = List.of("Tax refund", "Gift received", "Freelance work", "Sold item", "Cashback");
        for (int i = 0; i < randomInt(6, 14); i++) {
            final String description = incomeDescriptions.get(randomInt(0, incomeDescriptions.size()));
            transactions.add(newMoneyTransaction(DEBIT_ACCOUNT.getId(), description, description, "NL13BANK0007" + randomInt(100, 999),
                    randomDouble(120, 2600), randomDateBetween(FIVE_YEARS_AGO, NOW)));
        }

        return transactions;
    }

    private LocalDate randomDateBetween(final LocalDate start, final LocalDate end) {
        return start.plusDays(randomInt(0, (int) ChronoUnit.DAYS.between(start, end)));
    }

    private MoneyTransaction newMoneyTransaction(final long accountId, final String description, final String counterpartyName,
                                                 final String counterpartyNumber, final double amount, final LocalDate date) {
        final MoneyTransaction transaction = new MoneyTransaction();
        transaction.setUserId(USER_ID);
        transaction.setAccountId(accountId);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setTimestamp(date.atStartOfDay().toInstant(ZoneOffset.UTC));
        transaction.setCounterpartyAccountName(counterpartyName);
        transaction.setCounterpartyAccountNumber(counterpartyNumber);
        transaction.setCurrencyId(EURO_ID);
        transaction.setExchangeRate(1);
        return transaction;
    }

    private static final Random RANDOM = new Random();

    public static int randomInt(final int min, final int max) {
        return RANDOM.nextInt(max - min) + min;
    }

    protected static double randomDouble(final double min, final double max) {
        return RANDOM.nextDouble(max - min) + min;
    }
}
