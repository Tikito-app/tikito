package org.tikito.service.extractor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TRTPExtractorTest {

    @Test
    void keyValueTest() {
        final String input = "/TRTP/SEPA INCASSO ALGEMEEN DOORLOPEND/CSID/NL98ZZZ27223450000\r" +
                "/NAME/AEGON NEDERLAND/MARF/AEDE20241227H1437439/REMI/AEGON H14374\r" +
                "39 - TEL 058-2446650 CONTRACTUELE RENTE EN AFLOSSING  TERMIJN 01-\r" +
                "01 TOT 01-02/IBAN/NL96ABNA0450005678/BIC/ABNANL2A/EREF/E2E/H /H14\r" +
                "37439  /22-01-2025/PROL /";
        final Map<String, String> result = TRTPExtractor.extract(input);

        assertEquals("AEGON NEDERLAND", result.get("NAME"));
        assertEquals("NL96ABNA0450005678", result.get("IBAN"));
        assertEquals("ABNANL2A", result.get("BIC"));
    }
}