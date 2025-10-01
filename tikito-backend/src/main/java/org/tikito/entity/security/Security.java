package org.tikito.entity.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.security.SecurityDto;
import org.tikito.dto.security.SecurityType;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Security {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private SecurityType securityType;
    private Long currencyId;
    private String name;
    private String sector;
    private String industry;
    private String exchange;
    private String imageUrl;
    private String currentIsin;

    public Security(final SecurityDto dto) {
        this.id = dto.getId();
        this.securityType = dto.getSecurityType();
        this.currencyId = dto.getCurrencyId();
        this.name = dto.getName();
        this.sector = dto.getSector();
        this.industry = dto.getIndustry();
        this.exchange = dto.getExchange();
        this.imageUrl = dto.getImageUrl();
        this.currentIsin = dto.getCurrentIsin();
    }

    public SecurityDto toDto() {
        return new SecurityDto(
                id,
                securityType,
                currencyId,
                name,
                sector,
                industry,
                exchange,
                imageUrl,
                currentIsin);
    }
}
