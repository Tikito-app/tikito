package org.tikito.dto;

import org.tikito.dto.security.SecurityTransactionDto;
import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.entity.UserAccount;
import org.tikito.entity.security.SecurityTransaction;
import org.tikito.service.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SecurityTransactionDtoTest extends BaseTest {

    private static final Instant TIMESTAMP = Instant.now();
    private static final String ISIN = UUID.randomUUID().toString();
    private static final String PRODUCT_NAME = UUID.randomUUID().toString();
    private static final int AMOUNT = randomInt(30, 100);
    private static final double PRICE = randomDouble(10, 20);
    private static final String DESCRIPTION = UUID.randomUUID().toString();
    private static final SecurityTransactionType TRANSACTION_TYPE = SecurityTransactionType.BUY;
    private static final String COUNTRY = UUID.randomUUID().toString();
    private static final long ACCOUNT_ID = 2L;

    @BeforeEach
    void setup() {
        DEFAULT_USER_ACCOUNT = new UserAccount();
        DEFAULT_USER_ACCOUNT.setId(1L);
    }

    @Test
    void getUniqueKey_shouldEqual_givenEqualValues() {
        assertEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(ACCOUNT_ID, new SecurityTransactionImportLine(TIMESTAMP, ISIN, PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TRANSACTION_TYPE, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }

    @Test
    void getUniqueKey_shouldNotBeEqual_givenDifferentTimestamp() {
        assertNotEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(ACCOUNT_ID, new SecurityTransactionImportLine(TIMESTAMP.plusMillis(1), ISIN, PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TRANSACTION_TYPE, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }

    @Test
    void getUniqueKey_shouldNotBeEqual_givenDifferentIsin() {
        assertNotEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(ACCOUNT_ID, new SecurityTransactionImportLine(TIMESTAMP, UUID.randomUUID().toString(), PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TRANSACTION_TYPE, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }

    @Test
    void getUniqueKey_shouldNotBeEqual_givenDifferentAmount() {
        assertNotEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(ACCOUNT_ID, new SecurityTransactionImportLine(TIMESTAMP, ISIN, PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, 2, PRICE, DESCRIPTION, TRANSACTION_TYPE, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }

    @Test
    void getUniqueKey_shouldNotBeEqual_givenDifferentPrice() {
        assertNotEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(ACCOUNT_ID, new SecurityTransactionImportLine(TIMESTAMP, ISIN, PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, AMOUNT, 4, DESCRIPTION, TRANSACTION_TYPE, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }

    @Test
    void getUniqueKey_shouldNotBeEqual_givenDifferentTransactionType() {
        assertNotEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(ACCOUNT_ID, new SecurityTransactionImportLine(TIMESTAMP, ISIN, PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, SecurityTransactionType.SELL, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }

    @Test
    void getUniqueKey_shouldNotBeEqual_givenDifferentAccountIds() {
        assertNotEquals(
                SecurityTransactionDto.getUniqueKey(new SecurityTransaction(null, DEFAULT_USER_ACCOUNT.getId(), null, ISIN, ACCOUNT_ID, CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, TIMESTAMP, null, 1.0, TRANSACTION_TYPE)),
                SecurityTransactionDto.getUniqueKey(3L, new SecurityTransactionImportLine(TIMESTAMP, ISIN, PRODUCT_NAME, "EUR", CURRENCY_EURO_ID, AMOUNT, PRICE, DESCRIPTION, SecurityTransactionType.SELL, COUNTRY, null, null, 0, null, false, null, 1.0)));
    }
}