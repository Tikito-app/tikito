package org.tikito.dto.security;

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
    private long securityId;
    private LocalDate validFrom;
    private LocalDate validTo;
}
