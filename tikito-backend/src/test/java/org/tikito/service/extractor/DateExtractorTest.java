package org.tikito.service.extractor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateExtractorTest {

    @Test
    @Disabled // todo
    void extractDateDashed() {
        final String text = "text 2025-01-02 13:37 and other text";
        final Optional<Instant> timestamp = DateExtractor.extractDateDashed(text);
        assertEquals(Instant.parse("2025-01-03T13:37:00.000Z"), timestamp.get());
    }

    @Test
    @Disabled // todo
    void extractDateSlashed() {
        final String text = "text 2025-01-02 13:37 and other text";
        final Optional<Instant> timestamp = DateExtractor.extractDateDashed(text);
        assertEquals(Instant.parse("2025-01-03T13:37:00.000Z"), timestamp.get());
    }


}