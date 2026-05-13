package org.tikito.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void getDoubleOrDefault() {
        assertEquals(8921234, Util.getDoubleOrDefault("8,921,234.00", 0d));
        assertEquals(21234, Util.getDoubleOrDefault("21,234.00", 0d));
        assertEquals(21234, Util.getDoubleOrDefault("21.234,00", 0d));
        assertEquals(8921234, Util.getDoubleOrDefault("8.921.234,00", 0d));
        assertEquals(21234.5, Util.getDoubleOrDefault("21,234.50", 0d));
        assertEquals(21234.5, Util.getDoubleOrDefault("21.234,50", 0d));
    }
}