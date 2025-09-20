package org.tikito.service.MT940.lines;

import org.tikito.service.MT940.MT940Line;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MT940Line61Statement extends MT940Line {
    private final LocalDate valueDate;
    private final boolean credit;
    private double amount;

    public MT940Line61Statement(final String line) {
        super(line);

        valueDate = LocalDate.of(
                Integer.parseInt("20" + line.substring(0, 2)),
                Integer.parseInt(line.substring(2, 4)),
                Integer.parseInt(line.substring(4, 6)));
        credit = line.charAt(10) == 'C';
        int endPos = -1;
        for (int i = 11; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (!Character.isDigit(c) && ',' != c) {
                endPos = i;
                i = line.length();
            }
        }
        if (line.charAt(endPos - 1) == ',') {
            endPos--;
        }
        amount = Double.parseDouble(line.substring(11, endPos).replaceAll(",", "."));

        if (!credit) {
            amount = -amount;
        }
    }
}
