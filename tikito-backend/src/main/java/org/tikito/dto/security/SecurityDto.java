package org.tikito.dto.security;

import org.tikito.dto.IsinDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityDto {
    private Long id;
    private SecurityType securityType;
    private Long currencyId;
    private String name;
    private List<IsinDto> isins;
    private String sector;
    private String industry;
    private String exchange;
    private String imageUrl;

    public SecurityDto(final SecurityType securityType, final Long currencyId, final String isin, final String name) {
        final IsinDto isinDto = new IsinDto();
        isinDto.setIsin(isin);

        this.securityType = securityType;
        this.currencyId = currencyId;
        this.name = name;
        this.isins = new ArrayList<>();
        this.isins.add(isinDto);
        isinDto.setSecurityId(id);
    }

    public Optional<IsinDto> getIsin(final LocalDate start, final LocalDate end) {
        if (isins == null || isins.isEmpty()) {
            return Optional.empty();
        }

        for (final IsinDto isin : isins) {
            if (isin.isValid(start, end)) {
                return Optional.of(isin);
            }
        }
        return Optional.empty();
    }
}
