package org.tikito.entity.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.security.IsinDto;

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
    private long securityId;

    @Column(nullable = false)
    private LocalDate validFrom;
    private LocalDate validTo;

    public Isin(final String isin) {
        this.isin = isin;
    }

    public IsinDto toDto() {
        return new IsinDto(
                isin,
                symbol,
                securityId,
                validFrom,
                validTo);
    }
}
