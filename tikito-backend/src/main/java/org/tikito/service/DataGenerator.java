package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.controller.request.CreateOrUpdateMoneyTransactionGroupRequest;
import org.tikito.dto.AccountType;
import org.tikito.dto.money.MoneyTransactionField;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;
import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.Account;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.security.*;
import org.tikito.exception.EmailAlreadyExistsException;
import org.tikito.exception.PasswordNotLongEnoughException;
import org.tikito.repository.*;
import org.tikito.service.money.MoneyHoldingService;
import org.tikito.service.money.MoneyTransactionGroupService;
import org.tikito.service.security.SecurityHoldingService;
import org.tikito.service.security.SecurityService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

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
    private final SecurityPriceRepository securityPriceRepository;
    private final MoneyHoldingService moneyHoldingService;
    private final UserAccountService userAccountService;
    private final SecurityHoldingRepository securityHoldingRepository;
    private final CacheService cacheService;
    private final SecurityService securityService;

    private Account SAVINGS_ACCOUNT;
    private Account DEBIT_ACCOUNT;
    private Account STOCK_ACCOUNT;

    private Security SECURITY1;
    private Security SECURITY2;
    private Security SECURITY3;

    private final Map<Long, Map<LocalDate, SecurityPrice>> pricesPerDateAndSecurityId = new HashMap<>();

    public DataGenerator(final PlatformTransactionManager txManager, final SecurityTransactionRepository securityTransactionRepository,
                         final MoneyTransactionGroupService moneyTransactionGroupService,
                         final AccountRepository accountRepository,
                         final UserAccountService userAccountService,
                         final MoneyTransactionRepository moneyTransactionRepository,
                         final SecurityHoldingService securityHoldingService,
                         final SecurityRepository securityRepository,
                         final SecurityPriceRepository securityPriceRepository,
                         final MoneyHoldingService moneyHoldingService,
                         final UserAccountService userAccountService1,
                         final SecurityHoldingRepository securityHoldingRepository,
                         final CacheService cacheService, final SecurityService securityService) {
        this.securityTransactionRepository = securityTransactionRepository;
        this.moneyTransactionGroupService = moneyTransactionGroupService;

        this.accountRepository = accountRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
        this.securityHoldingService = securityHoldingService;
        this.securityRepository = securityRepository;
        this.securityPriceRepository = securityPriceRepository;
        this.moneyHoldingService = moneyHoldingService;

        this.userAccountService = userAccountService1;
        this.securityHoldingRepository = securityHoldingRepository;

        this.cacheService = cacheService;
        this.securityService = securityService;
    }

    public void generate() throws PasswordNotLongEnoughException, EmailAlreadyExistsException, IOException {
        userAccountService.register("demo", "demodemodemo");
        generateAccounts();
        EURO_ID = securityRepository.findAll().stream()
                .filter(security -> security.getName().equals("Euro"))
                .findFirst()
                .get()
                .getId();
        generateSecurities();
        securityService.updateSecurityPrices(SECURITY1.getId());
        pricesPerDateAndSecurityId.put(SECURITY1.getId(), new HashMap<>());
        securityPriceRepository.findAllBySecurityId(SECURITY1.getId())
                .forEach(price -> {
                    pricesPerDateAndSecurityId.get(SECURITY1.getId()).put(price.getDate(), price);
                });
//        generateSecurityPrices(SECURITY1, false);

        generateSecurityTransactions(SECURITY1, STOCK_ACCOUNT.getId());

        generateMoneyTransactionGroups();
        generateMoneyTransactions();

        securityHoldingService.recalculateHistoricalValue(USER_ID, SECURITY1.getId());
        securityHoldingService.recalculateAggregatedHistoricalHoldingValues(USER_ID);

        moneyTransactionGroupService.groupTransactions(USER_ID);
        moneyHoldingService.recalculateHistoricalHoldingValues(USER_ID, DEBIT_ACCOUNT.getId());
        moneyHoldingService.recalculateHistoricalHoldingValues(USER_ID, SAVINGS_ACCOUNT.getId());
        moneyHoldingService.recalculateAggregatedHistoricalHoldingValues(USER_ID);

        cacheService.refreshCurrencies();
        cacheService.refreshSecurities();
    }

    public void generateAccounts() {
        SAVINGS_ACCOUNT = new Account();
        SAVINGS_ACCOUNT.setAccountNumber("1234");
        SAVINGS_ACCOUNT.setAccountType(AccountType.DEBIT);
        SAVINGS_ACCOUNT.setUserId(USER_ID);
        SAVINGS_ACCOUNT.setName("Savings");
        SAVINGS_ACCOUNT.setCurrencyId(EURO_ID);
        accountRepository.saveAndFlush(SAVINGS_ACCOUNT);

        DEBIT_ACCOUNT = new Account();
        DEBIT_ACCOUNT.setAccountNumber("5678");
        DEBIT_ACCOUNT.setAccountType(AccountType.DEBIT);
        DEBIT_ACCOUNT.setUserId(USER_ID);
        DEBIT_ACCOUNT.setName("Debit");
        DEBIT_ACCOUNT.setCurrencyId(EURO_ID);
        accountRepository.saveAndFlush(DEBIT_ACCOUNT);

        STOCK_ACCOUNT = new Account();
        STOCK_ACCOUNT.setAccountNumber("9876");
        STOCK_ACCOUNT.setAccountType(AccountType.SECURITY);
        STOCK_ACCOUNT.setUserId(USER_ID);
        STOCK_ACCOUNT.setName("Stock portfolio");
        STOCK_ACCOUNT.setCurrencyId(EURO_ID);
        accountRepository.saveAndFlush(STOCK_ACCOUNT);
    }

    public void generateSecurities() {
        final Isin isin1 = new Isin();
        final Isin isin2 = new Isin();
        final Isin isin3 = new Isin();
        isin1.setIsin("NL0011540547");
        isin1.setSymbol("ABN.AS");

        SECURITY1 = new Security();
        SECURITY1.setName("Stock ABC");
        SECURITY1.setCurrencyId(EURO_ID);
        SECURITY1.setSecurityType(SecurityType.STOCK);
        SECURITY1.setIsins(new ArrayList<>(List.of(isin1)));
        SECURITY1.setIndustry("Tech");
        SECURITY1.setExchange("AMS");
        SECURITY1.setSector("Health");

        isin1.setSecurity(SECURITY1);

        SECURITY1 = securityRepository.saveAndFlush(SECURITY1);
//        SECURITY2 = securityRepository.saveAndFlush(SECURITY2);
//        SECURITY3 = securityRepository.saveAndFlush(SECURITY3);
    }

    public void generateSecurityPrices(final Security security, final boolean down) {
        final List<SecurityPrice> prices = new ArrayList<>();
        LocalDate currentDate = FIVE_YEARS_AGO;
        double lastPrice = randomDouble(50, 100);

        pricesPerDateAndSecurityId.put(security.getId(), new HashMap<>());

        while (currentDate.isBefore(NOW)) {
            lastPrice -= down ?
                    randomDouble(-0.02, 0.01) :
                    randomDouble(-0.01, 0.02);
            final SecurityPrice price = new SecurityPrice();
            price.setSecurityId(security.getId());
            price.setPrice(lastPrice);
            price.setDate(currentDate);

            pricesPerDateAndSecurityId.get(security.getId()).put(currentDate, price);
            prices.add(price);
            currentDate = currentDate.plusDays(1);
        }

        securityPriceRepository.saveAllAndFlush(prices);
    }

    public void generateSecurityTransactions(final Security security, final long accountId) {
        LocalDate currentDate = FIVE_YEARS_AGO;
        int totalAmount = 0;
        final List<SecurityTransaction> transactions = new ArrayList<>();

        final SecurityHolding holding = new SecurityHolding();
        holding.setSecurityId(security.getId());
        holding.setAccountIds(new HashSet<>(Set.of(STOCK_ACCOUNT.getId())));
        holding.setSecurityType(SecurityType.STOCK);
        holding.setUserId(USER_ID);
        holding.setCurrencyId(EURO_ID);
        securityHoldingRepository.saveAndFlush(holding);


        for (int i = 0; i < randomInt(5, 10); i++) {
            final SecurityTransaction transaction = new SecurityTransaction();
            transaction.setTimestamp(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            transaction.setIsin(security.getLatestIsin().getIsin());
            transaction.setAccountId(accountId);
            transaction.setUserId(USER_ID);
            transaction.setSecurityId(SECURITY1.getId());
            transaction.setCurrencyId(EURO_ID);
            transaction.setExchangeRate(1);

            int amount = randomInt(-50, 200);

            if (totalAmount + amount < 0) {
                amount = -totalAmount;
            }

            transaction.setPrice(pricesPerDateAndSecurityId.get(security.getId()).get(currentDate).getPrice() * (amount < 0 ? 1 : -1));
            totalAmount += amount;
            transaction.setTransactionType(amount < 0 ? SecurityTransactionType.SELL : SecurityTransactionType.BUY);
            transaction.setAmount(Math.abs(amount));

            if(transaction.getAmount() > 0) {
                transactions.add(transaction);
            }
            currentDate = currentDate.plusDays(randomInt(100, 200));
        }
        securityTransactionRepository.saveAllAndFlush(transactions);

    }

    public void generateMoneyTransactionGroups() {
        final CreateOrUpdateMoneyTransactionGroupRequest request = new CreateOrUpdateMoneyTransactionGroupRequest();
        request.setName("Groceries");
        request.setQualifiers(new ArrayList<>(List.of(
                new MoneyTransactionGroupQualifierDto(0, 0, MoneyTransactionGroupQualifierType.INCLUDES, "Supermarket", MoneyTransactionField.DESCRIPTION)
        )));
        moneyTransactionGroupService.createOrUpdateGroup(USER_ID, request);
    }

    public void generateMoneyTransactions() {
        LocalDate currentDate = FIVE_YEARS_AGO;

        final List<MoneyTransaction> transactions = new ArrayList<>();

        while (currentDate.isBefore(NOW)) {
            final MoneyTransaction transaction = new MoneyTransaction();
            transaction.setUserId(USER_ID);
            transaction.setDescription("Groceries for things");
            transaction.setAccountId(DEBIT_ACCOUNT.getId());
            transaction.setAmount(randomDouble(-150, -50));
            transaction.setTimestamp(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            transaction.setCounterpartAccountName("Supermarket");
            transaction.setCounterpartAccountNumber("NL13BANK0001234567");
            transaction.setCurrencyId(EURO_ID);
            transaction.setExchangeRate(1);
            transactions.add(transaction);
            currentDate = currentDate.plusDays(randomInt(5, 20));
        }

        currentDate = FIVE_YEARS_AGO;
        while (currentDate.isBefore(NOW)) {
            final MoneyTransaction transaction = new MoneyTransaction();
            transaction.setUserId(USER_ID);
            transaction.setDescription("My house payments");
            transaction.setAccountId(DEBIT_ACCOUNT.getId());
            transaction.setAmount(randomDouble(-2000, -1000));
            transaction.setTimestamp(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            transaction.setCounterpartAccountName("Mortgage");
            transaction.setCounterpartAccountNumber("NL13BANK0001234567");
            transaction.setCurrencyId(EURO_ID);
            transaction.setExchangeRate(1);
            transactions.add(transaction);
            currentDate = currentDate.plusMonths(1);
        }

        currentDate = FIVE_YEARS_AGO;
        while (currentDate.isBefore(NOW)) {
            final MoneyTransaction transaction = new MoneyTransaction();
            transaction.setUserId(USER_ID);
            transaction.setDescription("Payment from my boss");
            transaction.setAccountId(DEBIT_ACCOUNT.getId());
            transaction.setAmount(randomDouble(2000, 3000));
            transaction.setTimestamp(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            transaction.setCounterpartAccountName("Paycheck");
            transaction.setCounterpartAccountNumber("NL13BANK0005365");
            transaction.setCurrencyId(EURO_ID);
            transaction.setExchangeRate(1);
            transactions.add(transaction);
            currentDate = currentDate.plusMonths(1);
        }

        currentDate = FIVE_YEARS_AGO;
        while (currentDate.isBefore(NOW)) {
            final MoneyTransaction transaction = new MoneyTransaction();
            transaction.setUserId(USER_ID);
            transaction.setDescription("Shop " + randomInt(1, 100));
            transaction.setAccountId(DEBIT_ACCOUNT.getId());
            transaction.setAmount(randomDouble(-100, 50));
            transaction.setTimestamp(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            transaction.setCounterpartAccountName("Shop " + randomInt(1, 10));
            transaction.setCounterpartAccountNumber("NL13BANK0001234567");
            transaction.setCurrencyId(EURO_ID);
            transaction.setExchangeRate(1);
            transactions.add(transaction);
            currentDate = currentDate.plusDays(randomInt(1, 5));
        }

        moneyTransactionRepository.saveAllAndFlush(transactions);
    }

    public static int randomInt(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    protected static double randomDouble(final double min, final double max) {
        final Random random = new Random();
        return random.nextDouble(max - min) + min;
    }
}
