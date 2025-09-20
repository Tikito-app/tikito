package org.tikito.entity.security;

import org.tikito.dto.IsinDto;
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
public class Isin {
    @Id
    private String isin;
    private String symbol;

    @Column(nullable = false)
    private LocalDate validFrom;

    private LocalDate validTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "security_id")
    private Security security;

    public Isin(final Security security, final IsinDto dto) {
        this.isin = dto.getIsin();
        this.symbol = dto.getSymbol();
        this.validFrom = dto.getValidFrom();
        this.validTo = dto.getValidTo();
        this.security = security;
    }

    public IsinDto toDto() {
        return new IsinDto(
                isin,
                symbol,
                validFrom,
                validTo,
                security.getId());
    }
}
