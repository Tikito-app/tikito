package org.tikito.entity.security;

import org.tikito.dto.security.SecurityPriceDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SecurityPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long securityId;

    @Column(nullable = false)
    private LocalDate date;
    private double price;

    public SecurityPrice(final SecurityPriceDto dto) {
        this.securityId = dto.getSecurityId();
        this.date = dto.getDate();
        this.price = dto.getPrice();
    }

    public SecurityPriceDto toDto() {
        return new SecurityPriceDto(
                id,
                securityId,
                date,
                price);
    }
}
