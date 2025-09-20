package org.tikito.service.extractor;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyValueExtractorTest {

    @Test
    void keyValueTest() {
        final String input = "SEPA OVERBOEKING                 IBAN: NL13INGB0001234567\rBIC: INGBNL2A                    NAAM: HR TEST PERSON\rKENMERK: NOTPROVIDED";
        final Map<String, String> result = KeyValueExtractor.extract(input);

        assertEquals("NL13INGB0001234567", result.get("IBAN"));
        assertEquals("INGBNL2A", result.get("BIC"));
        assertEquals("HR TEST PERSON", result.get("NAAM"));
        assertEquals("NOTPROVIDED", result.get("KENMERK"));
    }
}