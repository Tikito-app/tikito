package org.tikito.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SecurityDto {
    private Long id;
    private SecurityType securityType;
    private Long currencyId;
    private String name;
    private String sector;
    private String industry;
    private String exchange;
    private String imageUrl;
    private String currentIsin;

    public SecurityDto(final SecurityType securityType, final Long currencyId, final String name) {
        this.securityType = securityType;
        this.currencyId = currencyId;
        this.name = name;
    }
}
