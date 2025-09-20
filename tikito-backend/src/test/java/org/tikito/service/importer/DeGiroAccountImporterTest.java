package org.tikito.service.importer;

import org.tikito.dto.security.SecurityTransactionImportLine;
import org.tikito.dto.security.SecurityTransactionImportResultDto;
import org.tikito.service.importer.security.DeGiroAccountImporter;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.tikito.dto.security.SecurityTransactionType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeGiroAccountImporterTest {

    private final DeGiroAccountImporter importer = new DeGiroAccountImporter();
    private final List<String> header = Arrays.asList("Datum\tTijd\tValutadatum\tProduct\tISIN\tOmschrijving\tFX\tMutatie\t\tSaldo\t\tOrder Id".split("\t"));

    @Test
    void testHeader() {
        assertTrue(importer.matchesHeader(header));
    }

    @Test
    void testMapEmptyLine() {
        final List<String> line = new ArrayList<>();
        final SecurityTransactionImportLine transaction = map(line);
        assertTrue(transaction.isFailed());
        assertEquals(SecurityTransactionImportResultDto.FAILED_INVALID_LINE, transaction.getFailedReason());
    }

    @Test
    void testMapADMIN_COSTS() {
        final List<String> line = Arrays.asList("03-01-2023\t14:00\t31-12-2022\t\t\tDEGIRO Aansluitingskosten 2023 (Nasdaq Stockholm AB - OMX)\t\tEUR\t-2.5\tEUR\t53.41\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);//
        assertEquals(ADMIN_COSTS, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapCASH_SWEEP() {
        final List<String> line = Arrays.asList("13-03-2025\t09:24\t13-03-2025\t\t\tDegiro Cash Sweep Transfer\t\tEUR\t-4.4\tEUR\t4.18\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(CASH_SWEEP, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapTHIRD_PARTY_COSTS() {
        final List<String> line = Arrays.asList("28-02-2025\t12:26\t28-02-2025\tWOLTERS KLUWER\tNL0000395903\tDEGIRO Transactiekosten en/of kosten van derden\t\tEUR\t-3\tEUR\t429.18\tf6d911c6-ab0a-409a-bc90-27bfbc1be0dd\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(THIRD_PARTY_COSTS, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapDIVIDEND() {
        final List<String> line = Arrays.asList("30-12-2024\t07:36\t27-12-2024\tVANGUARD S&P 500 UCITS ETF USD\tIE00B3XXRP09\tDividend\t\tUSD\t3.67\tUSD\t3.67\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(DIVIDEND, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapDIVIDENDFromKapitaal() {
        final List<String> line = Arrays.asList("20-12-2023\t13:27\t29-03-2022\tORCHID ISLAND CAPITAL INC\tUS68571X1037\tKapitaalsuitkering\t\tUSD\t11.08\tUSD\t10.56\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(DIVIDEND, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapDIVIDEND_TAX() {
        final List<String> line = Arrays.asList("12-12-2024\t07:28\t11-12-2024\tVANECK EUROPEAN EQUAL WEIGHT\tNL0010731816\tDividendbelasting\t\tEUR\t-1.11\tEUR\t-0.78\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(DIVIDEND_TAX, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapINTEREST() {
        final List<String> line = Arrays.asList("30-12-2023\t14:30\t31-12-2023\t\t\tFlatex Interest\t\tEUR\t-0.73\tEUR\t112.83\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(INTEREST, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapINTERESTFromIncome() {
        final List<String> line = Arrays.asList("05-10-2024\t20:01\t30-09-2024\t\t\tFlatex Interest Income\t\tEUR\t0\tEUR\t100.33\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(INTEREST, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapFLATEX_DEPOSIT() {
        final List<String> line = Arrays.asList("13-11-2023\t15:00\t14-11-2023\t\t\tflatex terugstorting\t\tEUR\t-3400\tEUR\t-3305.96\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(FLATEX_DEPOSIT, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapIDEAL_DEPOSIT() {
        final List<String> line = Arrays.asList("03-05-2023\t05:21\t02-05-2023\t\t\tiDEAL Deposit\t\tEUR\t1000\tEUR\t202.85\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(IDEAL_DEPOSIT, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapWITHDRAWAL() {
        final List<String> line = Arrays.asList("06-03-2025\t09:40\t06-03-2025\t\t\tProcessed Flatex Withdrawal\t\tEUR\t420\tEUR\t4.18\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(WITHDRAWAL, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapIDEAL_RESERVATION() {
        final List<String> line = Arrays.asList("01-03-2025\t11:53\t28-02-2025\t\t\tReservation iDEAL / Sofort Deposit\t\tEUR\t-6750\tEUR\t-6320.82\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(IDEAL_RESERVATION, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapCURRENCY_CREDITING() {
        final List<String> line = Arrays.asList("01-03-2025\t11:53\t28-02-2025\t\t\tReservation iDEAL / Sofort Deposit\t\tEUR\t-6750\tEUR\t-6320.82\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(IDEAL_RESERVATION, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapCURRENC_DEBITING() {
        final List<String> line = Arrays.asList("27-02-2025\t19:22\t27-02-2025\tALPHABET INC CLASS A\tUS02079K3059\tValuta Debitering\t\tEUR\t-2473.17\tEUR\t2922.68\t1b23777f-fb31-48de-bd41-fd0ecf6be09b\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(CURRENCY_DEBITING, transaction.getTransactionType(), transaction.getFailedReason());
    }

    @Test
    void testMapCOUNTRY_TAX() {
        final List<String> line = Arrays.asList("26-02-2025\t10:04\t26-02-2025\tUNIBAIL-RODAMCO-WE\tFR0013326246\tTransactiebelasting Frankrijk\t\tEUR\t-21\tEUR\t5509.02\tedef6b35-ce08-4d62-b1fa-ceabe3b7d8c1\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(COUNTRY_TAX, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals("Frankrijk", transaction.getCountry());
    }

    @Test
    void testMapBUY() {
        final List<String> line = Arrays.asList("26-02-2025\t10:11\t26-02-2025\tABN AMRO BANK NV\tNL0011540547\tKoop 244 @ 18,37 EUR\t\tEUR\t-4482.28\tEUR\t9926.74\t0551cb0d-771e-4f7f-a8c1-179c26008496\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(BUY, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(244, transaction.getAmount());
        assertEquals(-18.37, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testMapSELL() {
        final List<String> line = Arrays.asList("10-11-2023\t16:20\t10-11-2023\tALPHABET INC CLASS A\tUS02079K3059\tVerkoop 15 @ 130,75 USD\t\tUSD\t1961.25\tUSD\t1961.25\tf0fa562c-40e3-4e33-be6a-ce5410cf59f7\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(SELL, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(15, transaction.getAmount());
        assertEquals(130.75, transaction.getPrice());
        assertEquals("USD", transaction.getCurrency());
    }

    @Test
    void testMapTRANSFER_FROM() {
        final List<String> line = Arrays.asList("03-05-2023\t17:04\t03-05-2023\t\t\tOverboeking van uw geldrekening bij flatexDEGIRO Bank 2.797,15 EUR\t\t\t\tEUR\t202.85\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(TRANSFER_FROM, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(2797.15, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testMapTRANSFER_TO() {
        final List<String> line = Arrays.asList("15-11-2023\t23:32\t15-11-2023\t\t\tOverboeking naar uw geldrekening bij flatexDEGIRO Bank: 3.400 EUR\t\t\t\tEUR\t94.04\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(TRANSFER_TO, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(3400, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testMapSELL_NEW_ISIN() {
        final List<String> line = Arrays.asList("31-01-2022\t06:45\t31-01-2022\tROYAL DUTCH SHELL A\tGB00B03MLX29\tWIJZIGING ISIN: Verkoop 65 @ 22,795 EUR\t\tEUR\t1481.68\tEUR\t1617.94\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(SELL_ISIN_CHANGE, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(22.795, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testMapBUY_NEW_ISIN() {
        final List<String> line = Arrays.asList("31-01-2022\t06:45\t31-01-2022\tROYAL DUTCH SHELL A\tGB00B03MLX29\tWIJZIGING ISIN: Koop 65 @ 22,795 EUR\t\tEUR\t1481.68\tEUR\t1617.94\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(BUY_ISIN_CHANGE, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(-22.795, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testMapSELL_NEW_ISIN_variant() {
        final List<String> line = Arrays.asList("31-01-2022\t06:45\t31-01-2022\tROYAL DUTCH SHELL A\tGB00B03MLX29\tPRODUCTWIJZIGING : Verkoop 65 @ 22,795 EUR\t\tEUR\t1481.68\tEUR\t1617.94\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(SELL_PRODUCT_CHANGE, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(22.795, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testMapBUY_NEW_ISIN_variant() {
        final List<String> line = Arrays.asList("31-01-2022\t06:45\t31-01-2022\tROYAL DUTCH SHELL A\tGB00B03MLX29\tPRODUCTWIJZIGING : Koop 65 @ 22,795 EUR\t\tEUR\t1481.68\tEUR\t1617.94\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(BUY_PRODUCT_CHANGE, transaction.getTransactionType(), transaction.getFailedReason());
        assertEquals(-22.795, transaction.getPrice());
        assertEquals("EUR", transaction.getCurrency());
    }

    @Test
    void testGenericMapping() {
        final List<String> line = Arrays.asList("04-05-2023\t11:54\t04-05-2023\tSHELL PLC\tGB00BP6MXD84\tVerkoop 65 @ 27,08 EUR\t\tEUR\t1760.2\tEUR\t2770.27\tc54d8129-af48-41f0-b05e-4edbd6554197\n".split("\t"));
        final SecurityTransactionImportLine transaction = map(line);
        assertEquals(LocalDateTime.of(2023, 5, 4, 11, 54).toInstant(ZoneOffset.UTC), transaction.getTimestamp());
        assertEquals("GB00BP6MXD84", transaction.getIsin());
        assertEquals("SHELL PLC", transaction.getProductName());
        assertEquals("Verkoop 65 @ 27,08 EUR", transaction.getDescription());
    }

    private SecurityTransactionImportLine map(final List<String> line) {
        final List<List<String>> lines = new ArrayList<>();
        lines.add(line);
        final SecurityTransactionImportResultDto result = new SecurityTransactionImportResultDto(lines);
        importer.map(result.getLines().getFirst());
        return result.getLines().getFirst();
    }
}