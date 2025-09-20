package org.tikito.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IsinDto {

    private String isin;
    private String symbol;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Long securityId;

    public boolean isValid(final LocalDate start, final LocalDate end) {
        if (validFrom == null) {
            return true;
        } else if (start == null) {
            return false;
        }
        if (validFrom.isAfter(start)) {
            return false;
        }

        if (validTo == null || (end == null && !start.isAfter(validTo))) {
            return true;
        } else if (end == null) {
            return false;
        }

        return validTo.isAfter(end);
    }
}
