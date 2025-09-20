package org.tikito.entity.security;

import org.tikito.dto.security.SecurityDto;
import org.tikito.dto.security.SecurityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "security", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Isin> isins;

    private String sector;
    private String industry;
    private String exchange;
    private String imageUrl;

    public Security(final SecurityDto dto) {
        this.id = dto.getId();
        this.securityType = dto.getSecurityType();
        this.currencyId = dto.getCurrencyId();
        this.name = dto.getName();
        this.isins = new ArrayList<>(dto.getIsins().stream().map(isin -> new Isin(this, isin)).toList());
        this.sector = dto.getSector();
        this.industry = dto.getIndustry();
        this.exchange = dto.getExchange();
        this.imageUrl = dto.getImageUrl();
    }

    public Security(final String isin) {
        final Isin isinDto = new Isin();
        isinDto.setIsin(isin);

        this.isins = new ArrayList<>();
        this.isins.add(isinDto);
        isinDto.setSecurity(this);
    }

    public SecurityDto toDto() {
        return new SecurityDto(
                id,
                securityType,
                currencyId,
                name,
                new ArrayList<>(isins.stream().map(Isin::toDto).toList()),
                sector,
                industry,
                exchange,
                imageUrl);
    }

    public Isin onNewIsin(final LocalDate timestamp, final String isin, final String nextIsin) {
        final Isin oldIsin = isins.stream().filter(i -> i.getIsin().equals(isin)).findFirst().get();
        oldIsin.setValidTo(timestamp.minusDays(1));

        final Isin newIsinDto = new Isin(nextIsin, oldIsin.getSymbol(), timestamp, null, this);
        isins.add(newIsinDto);
        return newIsinDto;
    }

    public Isin getLatestIsin() {
        return isins
                .stream()
                .max((o1, o2) -> {
                    if (o1.getValidFrom() == o2.getValidFrom()) {
                        return 0;
                    } else if (o1.getValidFrom() == null) {
                        return -1;
                    } else if (o2.getValidFrom() == null) {
                        return 1;
                    }
                    return o1.getValidFrom().compareTo(o2.getValidFrom());
                })
                .get();
    }
}
