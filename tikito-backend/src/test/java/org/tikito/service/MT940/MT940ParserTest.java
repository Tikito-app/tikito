package org.tikito.service.MT940;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MT940ParserTest {
    @Test
    void testParseBlockSingleTransaction() {
        final String block = "ABNANL2A\n" +
                "940\n" +
                "ABNANL2A\n" +
                ":20:ABN AMRO BANK NV\n" +
                ":25:234564321\n" +
                ":28:31701/1\n" +
                ":60F:C241111EUR0,\n" +
                ":61:2411121112C250,N654NONREF\n" +
                ":86:SEPA OVERBOEKING                 IBAN: NL13INGB0001234567\r" +
                "BIC: INGBNL2A                    NAAM: HR TEST PERSON\r" +
                "OMSCHRIJVING: RENT + DEPOSIT     KENMERK: NOTPROVIDED\n" +
                ":62F:C241112EUR250,";
        final List<MT940Transaction> transactions = MT940Parser.parse(block);

        assertEquals(1, transactions.size());
        assertEquals(250, transactions.getFirst().getAmount());
        assertEquals(LocalDate.of(2024, 11, 12), transactions.getFirst().getDate());
        assertEquals("NL13INGB0001234567", transactions.getFirst().getToAccountNumber());
        assertEquals("HR TEST PERSON", transactions.getFirst().getToAccountName());
        assertEquals("RENT + DEPOSIT", transactions.getFirst().getDescription());
    }

    @Test
    void testParseBlockTRTP() {
        final String block = "ABNANL2A\n" +
                "940\n" +
                "ABNANL2A\n" +
                ":20:ABN AMRO BANK NV\n" +
                ":25:234564321\n" +
                ":28:31701/1\n" +
                ":60F:C241111EUR0,\n" +
                ":61:2411121112C250,N654NONREF\n" +
                ":86:/TRTP/SEPA INCASSO ALGEMEEN DOORLOPEND/CSID/NL98ZZZ27223450000\r" +
                "/NAME/AEGON NEDERLAND/MARF/AEDE20241227H1437439/REMI/AEGON H14374\r" +
                "39 - TEL 058-2446650 CONTRACTUELE RENTE EN AFLOSSING  TERMIJN 01-\r" +
                "01 TOT 01-02/IBAN/NL96ABNA0450005678/OMSCHRIJVING/TEST/BIC/ABNANL2A/EREF/E2E/H /H14\n" +
                "37439  /22-01-2025/PROL /" +
                ":62F:C241112EUR250,";
        final List<MT940Transaction> transactions = MT940Parser.parse(block);

        assertEquals(1, transactions.size());
        assertEquals(250, transactions.getFirst().getAmount());
        assertEquals(LocalDate.of(2024, 11, 12), transactions.getFirst().getDate());
        assertEquals("NL96ABNA0450005678", transactions.getFirst().getToAccountNumber());
        assertEquals("AEGON NEDERLAND", transactions.getFirst().getToAccountName());
        assertEquals("TEST", transactions.getFirst().getDescription());
    }

    @Test
    void testParseBlockMultipleTransaction() {
        final String block = "ABNANL2A\n" +
                "940\n" +
                "ABNANL2A\n" +
                ":20:ABN AMRO BANK NV\n" +
                ":25:234564321\n" +
                ":28:32401/1\n" +
                ":60F:C241118EUR221,75\n" +
                ":61:2411191119D3,14N426NONREF\n" +
                ":86:BEA, BETAALPAS                   ALBERT HEIJN 1234,PAS196\r" +
                "NR:NQ9NRN, 19.11.24/10:42        Amsterdam\n" +
                ":61:2411201119D11,01N426NONREF\n" +
                ":86:BEA, BETAALPAS                   ALBERT HEIJN 1234,PAS196\r" +
                "NR:DR04JS, 19.11.24/18:28        Amsterdam\n" +
                ":62F:C241119EUR207,6";
        final List<MT940Transaction> transactions = MT940Parser.parse(block);

        assertEquals(2, transactions.size());

        assertEquals(-3.14, transactions.getFirst().getAmount());
        assertEquals(LocalDate.of(2024, 11, 19), transactions.getFirst().getDate());
        assertNull(transactions.get(0).getToAccountNumber());
        assertNull(transactions.get(0).getToAccountName());

        assertEquals(-11.01, transactions.get(1).getAmount());
        assertEquals(LocalDate.of(2024, 11, 20), transactions.get(1).getDate());
        assertNull(transactions.get(1).getToAccountNumber());
        assertNull(transactions.get(1).getToAccountName());
    }

    @Test
    void testParseMultipleBlocks() {
        final String block = "ABNANL2A\n" +
                "940\n" +
                "ABNANL2A\n" +
                ":20:ABN AMRO BANK NV\n" +
                ":25:234564321\n" +
                ":28:32501/1\n" +
                ":60F:C241119EUR207,6\n" +
                ":61:2411201120D51,12N426NONREF\n" +
                ":86:BEA, BETAALPAS                   ALBERT HEIJN 1234,PAS196\r" +
                "NR:NP5S1F, 20.11.24/18:36        Amsterdam\n" +
                ":62F:C241120EUR156,48\n" +
                "-\n" +
                "ABNANL2A\n" +
                "940\n" +
                "ABNANL2A\n" +
                ":20:ABN AMRO BANK NV\n" +
                ":25:234564321\n" +
                ":28:32601/1\n" +
                ":60F:C241120EUR156,48\n" +
                ":61:2411211121C2750,N654NONREF\n" +
                ":86:SEPA OVERBOEKING                 IBAN: LT123450012345678987\r" +
                "BIC: REVOLT21XXX                 NAAM: MY NAME\r" +
                "OMSCHRIJVING: RENT + DEPOSIT     KENMERK: NOTPROVIDED\n" +
                ":62F:C241121EUR2906,48";
        final List<MT940Transaction> transactions = MT940Parser.parse(block);

        assertEquals(2, transactions.size());

        final MT940Transaction transaction0 = transactions.getFirst();
        assertEquals(-51.12, transaction0.getAmount());
        assertEquals(LocalDate.of(2024, 11, 20), transaction0.getDate());
        assertNull(transaction0.getToAccountNumber());
        assertNull(transaction0.getToAccountName());

        final MT940Transaction transaction1 = transactions.get(1);
        assertEquals(2750, transaction1.getAmount());
        assertEquals(LocalDate.of(2024, 11, 21), transaction1.getDate());
        assertEquals("LT123450012345678987", transaction1.getToAccountNumber());
        assertEquals("MY NAME", transaction1.getToAccountName());
        assertEquals("RENT + DEPOSIT", transaction1.getDescription());
    }
}