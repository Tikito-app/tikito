package org.tikito.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class SecurityPriceDto {
    private Long id;
    private Long securityId;
    private LocalDate date;
    private double price;

    public SecurityPriceDto(final Long securityId, final LocalDate date, final double price) {
        this.securityId = securityId;
        this.date = date;
        this.price = price;
    }
}
