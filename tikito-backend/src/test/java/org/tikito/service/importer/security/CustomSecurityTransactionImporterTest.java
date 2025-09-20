package org.tikito.service.importer.security;

import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.dto.security.SecurityTransactionType;
import org.tikito.service.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tikito.service.importer.security.CustomSecurityHeaderName.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomSecurityTransactionImporterTest extends BaseTest {
    private CustomSecurityTransactionImporter importer;

    @BeforeEach
    void setup() {
        final Map<String, Integer> config = new HashMap<>();
        config.put(DATE, 0);
        config.put(TIME, 1);
        config.put(ISIN, 2);
        config.put(AMOUNT, 3);
        config.put(PRICE, 4);
        config.put(TRANSACTION_COST, 5);
        config.put(ADMIN_COST, 6);
        config.put(CURRENCY, 7);
        config.put(EXCHANGE_RATE, 8);
        config.put(BUY_SELL, 9);

        importer = new CustomSecurityTransactionImporter(config, "", "", "", "");
    }

    @Test
    void map_shouldSucceed_givenValidData() {
        final List<SecurityTransactionImportLine> lines = importer.map(generateLine());

        final SecurityTransactionImportLine buyLine = lines.get(0);
        final SecurityTransactionImportLine transactionCostLine = lines.get(1);
        final SecurityTransactionImportLine adminCostLine = lines.get(2);

        assertBaseEquals(buyLine);
        assertBaseEquals(transactionCostLine);
        assertBaseEquals(adminCostLine);

        assertEquals(5, buyLine.getAmount());

        assertEquals(-2.356, buyLine.getPrice());
        assertEquals(SecurityTransactionType.BUY, buyLine.getTransactionType());

        assertEquals(-1.378, transactionCostLine.getPrice());
        assertEquals(0, transactionCostLine.getAmount());
        assertEquals(SecurityTransactionType.TRANSACTION_COST, transactionCostLine.getTransactionType());

        assertEquals(-8.6234, adminCostLine.getPrice());
        assertEquals(0, adminCostLine.getAmount());
        assertEquals(SecurityTransactionType.ADMIN_COSTS, adminCostLine.getTransactionType());
    }

    @Test
    void map_shouldFail_givenInvalidDate() {
        final SecurityTransactionImportLine lineToMap = generateLine();
        lineToMap.getCells().set(0, "dsf");
        final SecurityTransactionImportLine line = importer.map(lineToMap).getFirst();
        assertTrue(line.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_NO_VALID_TIMESTAMP, line.getFailedReason());
    }

    @Test
    void map_shouldFail_givenInvalidTime() {
        final SecurityTransactionImportLine lineToMap = generateLine();
        lineToMap.getCells().set(1, "dsf");
        final SecurityTransactionImportLine line = importer.map(lineToMap).getFirst();
        assertTrue(line.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_NO_VALID_TIMESTAMP, line.getFailedReason());
    }

    @Test
    void map_shouldFail_givenInvalidAmount() {
        final SecurityTransactionImportLine lineToMap = generateLine();
        lineToMap.getCells().set(3, "sdf");
        final SecurityTransactionImportLine line = importer.map(lineToMap).getFirst();
        assertTrue(line.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_NO_AMOUNT, line.getFailedReason());
    }

    @Test
    void map_shouldFail_givenInvalidPrice() {
        final SecurityTransactionImportLine lineToMap = generateLine();
        lineToMap.getCells().set(4, "sdf");
        final SecurityTransactionImportLine line = importer.map(lineToMap).getFirst();
        assertTrue(line.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_NO_PRICE, line.getFailedReason());
    }

    @Test
    void map_shouldFail_givenInvalidTransactionCost() {
        final SecurityTransactionImportLine lineToMap = generateLine();
        lineToMap.getCells().set(5, "sdf");
        final SecurityTransactionImportLine line = importer.map(lineToMap).getFirst();
        assertTrue(line.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_NO_TRANSACTION_COST, line.getFailedReason());
    }

    @Test
    void map_shouldFail_givenInvalidAdminCost() {
        final SecurityTransactionImportLine lineToMap = generateLine();
        lineToMap.getCells().set(6, "sdf");
        final SecurityTransactionImportLine line = importer.map(lineToMap).getFirst();
        assertTrue(line.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_NO_ADMIN_COST, line.getFailedReason());
    }

    private void assertBaseEquals(final SecurityTransactionImportLine line) {
        assertEquals(Instant.parse("2025-02-01T13:37:00.000Z"), line.getTimestamp());
        assertEquals(ISIN_ONE, line.getIsin());
        assertEquals("USD", line.getCurrency());
    }

    private SecurityTransactionImportLine generateLine() {
        final SecurityTransactionImportLine line = new SecurityTransactionImportLine();
        line.setCells(new ArrayList<>());
        line.getCells().add("01-02-2025");
        line.getCells().add("13:37");
        line.getCells().add(ISIN_ONE);
        line.getCells().add("5");
        line.getCells().add("2.356");
        line.getCells().add("-1.378");
        line.getCells().add("-8.6234");
        line.getCells().add("USD");
        line.getCells().add("1.3542");
        line.getCells().add("BUY");
        return line;
    }
}