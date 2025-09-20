package org.tikito.service.MT940.lines;

import org.junit.jupiter.api.Test;

class MT940Line61StatementTest {
    @Test
    void testParse() {
        final String line = "2504260426C2331,N654NONREF";
        final MT940Line61Statement statement = new MT940Line61Statement(line);

    }
}