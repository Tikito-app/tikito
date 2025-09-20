package org.tikito.service;

import org.junit.jupiter.api.Assertions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.tikito.auth.AuthUser;
import org.tikito.auth.PasswordUtil;
import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.UserAccount;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.service.security.SecurityCalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@ActiveProfiles("test")
public class BaseTest {
    public static final Instant NOW_TIME = Instant.now();
    public static final LocalDate NOW = LocalDate.of(2025, 4, 27);
    public static final LocalDate ONE_YEAR_AGO = NOW.minusDays(365);
    public static final LocalDate FIVE_YEARS_AGO = NOW.minusDays(365 * 5);
    public static final LocalDate TWENTY_YEARS_AGO = NOW.minusDays(365 * 20);

    public static long CURRENCY_EURO_ID = 1L;
    public static long CURRENCY_DOLLAR_ID = 2L;

    public static final String ACCOUNT_NAME_ONE = "Test account";
    public static final String ACCOUNT_NAME_TWO = "Test Dollar account";
    public static final String ACCOUNT_NUMBER_ONE = "12345";
    public static final String ACCOUNT_NUMBER_TWO = "67890";

    public static final String COUNTERPART_ACCOUNT_NUMBER = "54321";
    public static final String COUNTERPART_ACCOUNT_NAME = "Other test counterpart account";

    public static final String ISIN_ONE = "NL0000395903"; // WOLTERS KLUWER
    public static final String ISIN_ONE_OLD = "NL0000395903-old"; // WOLTERS KLUWER
    public static final String ISIN_TWO = "US02079K3059"; // ALPHABET INC. - CLASS A
    public static final String ISIN_THREE = "US0231351067"; // AMAZON.COM INC. - COM

    protected static UserAccount DEFAULT_USER_ACCOUNT = null;
    protected static final String DEFAULT_USER_ACCOUNT_PASSWORD = randomString(20);

    protected static MoneyTransaction DEFAULT_MONEY_TRANSACTION_ONE;
    protected static MoneyTransaction DEFAULT_MONEY_TRANSACTION_TWO;
    protected static MoneyTransaction DEFAULT_MONEY_TRANSACTION_THREE;

    protected static HistoricalSecurityHoldingValueDto randomHistoricalHoldingValueDto(final long currencyId) {
        final HistoricalSecurityHoldingValueDto value = HistoricalSecurityHoldingValueDto
                .builder()
                .accountIds(Set.of())
                .currencyId(currencyId)
                .currencyMultiplier(randomDouble(1, 2))
                .date(LocalDate.now().minusDays(randomInt(0, 1000)))
                .price(randomDouble(1, 100))
                .amount(randomInt(10, 100))
                .totalAdministrativeCosts(randomDouble(1, 10))
                .totalCashInvested(randomDouble(500, 1000))
                .totalDividend(randomDouble(20, 50))
                .totalTaxes(randomDouble(1, 10))
                .totalTransactionCosts(randomDouble(1, 10))
                .totalCashWithdrawn(randomDouble(1, 10))
                .build();
        SecurityCalculator.calculateWorth(value);
        return value;
    }

    protected static void loginWithUser(final long userId) {
        final AuthUser authUser = new AuthUser();
        authUser.setId(userId);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(authUser, null, new ArrayList<>()));
    }

    protected static int randomInt(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    protected static double randomDouble(final double min, final double max) {
        final Random random = new Random();
        return random.nextDouble(max - min) + min;
    }

    protected static String randomString(final int length) {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'
        final Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    protected static String randomEmail() {
        return randomString(10) + "@" + randomString(10) + ".com";
    }

    protected MockMultipartFile getClassPathResourceToImport(final String path, final String uploadFilename) throws IOException {
        final InputStream inputStream = new ClassPathResource(path).getInputStream();
        final String csv = readFromInputStream(inputStream);
        return new MockMultipartFile(
                "file",
                uploadFilename,
                MediaType.TEXT_PLAIN_VALUE,
                csv.getBytes());
    }

    protected String getClassPathResource(final String path) throws IOException {
        final InputStream inputStream = new ClassPathResource(path).getInputStream();
        return readFromInputStream(inputStream);
    }

    protected String readFromInputStream(final InputStream inputStream)
            throws IOException {
        final StringBuilder resultStringBuilder = new StringBuilder();
        try (final BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    protected Isin isin(final String isinString, final LocalDate validFrom, final String symbol) {
        return isin(isinString, validFrom, null, symbol);
    }

    protected Isin isin(final String isinString, final LocalDate validFrom, final LocalDate validTo, final String symbol) {
        final Isin isin = new Isin();
        isin.setIsin(isinString);
        isin.setValidFrom(validFrom);
        isin.setValidTo(validTo);
        isin.setSymbol(symbol);
        return isin;
    }

    protected Security security(final String name, final long currencyId, final List<Isin> isins) {
        final Security security = new Security();
        security.setName(name);
        security.setIsins(isins);
        security.getIsins().forEach(isin -> isin.setSecurity(security));
        security.setCurrencyId(currencyId);
        security.setSecurityType(SecurityType.STOCK);
        return security;
    }

    protected MoneyTransaction moneyTransaction(final long userId,
                                                final long accountId,
                                                final Instant timestamp,
                                                final long currencyId,
                                                final double amount,
                                                final double finalBalance,
                                                final String counterpartAccountNumber,
                                                final String counterpartAccountName) {
        final MoneyTransaction transaction = new MoneyTransaction();
        transaction.setUserId(userId);
        transaction.setAccountId(accountId);
        transaction.setTimestamp(timestamp);
        transaction.setCurrencyId(currencyId);
        transaction.setCounterpartAccountNumber(counterpartAccountNumber);
        transaction.setCounterpartAccountName(counterpartAccountName);
        transaction.setAmount(amount);
        transaction.setFinalBalance(finalBalance);
        return transaction;
    }

    protected UserAccount userAccount(final String email, final String password) {
        return userAccount(email, password, null);
    }

    protected UserAccount userAccount(final String email, final String password, final String activationCode) {
        final UserAccount userAccount = new UserAccount();
        userAccount.setEmail(email);
        userAccount.setPassword(PasswordUtil.createBcryptHash(password.toCharArray()));
        if (activationCode != null) {
            userAccount.setActivationCode(activationCode);
            userAccount.setActivated(false);
        } else {
            userAccount.setActivated(true);
            userAccount.setActivationCode(randomString(20));
        }
        return userAccount;
    }

    protected void assertDoubleEquals(final double d1, final double d2) {
        assertDoubleEquals(d1, d2, 5);
    }

    protected void assertDoubleEquals(final double d1, final double d2, final int precision) {
        Assertions.assertEquals(
                new BigDecimal(d1).setScale(precision, RoundingMode.HALF_EVEN).doubleValue(),
                new BigDecimal(d2).setScale(precision, RoundingMode.HALF_EVEN).doubleValue());
    }
}
