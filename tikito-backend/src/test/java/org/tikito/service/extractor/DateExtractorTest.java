package org.tikito.service.extractor;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateExtractorTest {

    @Test
    void extractDateDashedIsoWithTime() {
        final String text = "text 2025-01-03 13:37 and other text";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2025-01-03T13:37:00.000Z"), timestamp.get());
    }

    @Test
    void extractDateSlashedYearFirstWithTime() {
        final String text = "text 2025/01/03 13:37 and other text";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2025-01-03T13:37:00.000Z"), timestamp.get());
    }

    @Test
    void extractDateDottedDayFirstWithSlashTime() {
        final String text = "Kenmerk: 17.11.24/10:42 703091";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2024-11-17T10:42:00.000Z"), timestamp.get());
    }

    @Test
    void extractDateDayFirstSlashedWithoutTime() {
        final String text = "invoice dated 03/01/2025 for services rendered";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2025-01-03T00:00:00.000Z"), timestamp.get());
    }

    @Test
    void extractDateDottedDayFirstTwoDigitYear() {
        final String text = "NR:NQ9NRN, 19.11.24/11:43 Amsterdam";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2024-11-19T11:43:00.000Z"), timestamp.get());
    }

    @Test
    void extractDateWithSecondsInTime() {
        final String text = "logged at 2025-06-15T08:09:10 by system";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2025-06-15T08:09:10.000Z"), timestamp.get());
    }

    @Test
    void extractDateSwapsMonthAndDayWhenMonthOutOfRange() {
        final String text = "some text 05-13-2025 more text";
        final Optional<Instant> timestamp = DateExtractor.extractDate(text);
        assertEquals(Instant.parse("2025-05-13T00:00:00.000Z"), timestamp.get());
    }

    @Test
    void extractDateReturnsEmptyWhenNoDatePresent() {
        final String text = "SEPA Overboeking IBAN: NL13INGB0001234567 BIC: INGBNL2A Naam: Hr Test Person";
        assertTrue(DateExtractor.extractDate(text).isEmpty());
    }

    @Test
    void extractDateReturnsEmptyForNullInput() {
        assertTrue(DateExtractor.extractDate(null).isEmpty());
    }
}
