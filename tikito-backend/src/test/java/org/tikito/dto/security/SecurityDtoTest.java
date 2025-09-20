package org.tikito.dto.security;

import org.tikito.service.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityDtoTest extends BaseTest {

    @Test
    void getIsin_shouldReturnCorrectIsin_givenDate() {
        final SecurityDto dto = security("test", 0L, List.of(
                        isin("i1", ONE_YEAR_AGO.minusDays(5), ONE_YEAR_AGO.minusDays(3), "s1"),
                        isin("i2", ONE_YEAR_AGO.minusDays(2), ONE_YEAR_AGO.plusDays(1), "s2")
        )).toDto();

        assertTrue(dto.getIsin(ONE_YEAR_AGO.minusDays(6), null).isEmpty());
        assertEquals("s1", dto.getIsin(ONE_YEAR_AGO.minusDays(5), null).get().getSymbol());
        assertEquals("s1", dto.getIsin(ONE_YEAR_AGO.minusDays(3), null).get().getSymbol());
        assertEquals("s2", dto.getIsin(ONE_YEAR_AGO.minusDays(2), null).get().getSymbol());
        assertEquals("s2", dto.getIsin(ONE_YEAR_AGO.plusDays(1), null).get().getSymbol());
        assertTrue(dto.getIsin(ONE_YEAR_AGO.plusDays(2), null).isEmpty());

    }
}