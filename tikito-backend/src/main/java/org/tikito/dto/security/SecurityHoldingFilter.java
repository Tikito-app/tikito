package org.tikito.dto.security;

import jakarta.validation.constraints.NotNull;
import org.tikito.dto.DateRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.util.Util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class SecurityHoldingFilter {
    private Set<@NotNull Long> accountIds;
    private Set<@NotNull Long> securityIds;
    private DateRange dateRange;
    private LocalDate startDate;

    public Set<Long> getAccountIds() {
        return Util.toSetOfNonNullLongs(accountIds);
    }

    public Set<Long> getSecurityIds() {
        return Util.toSetOfNonNullLongs(securityIds);
    }

    public Instant getStartDateAsInstant() {
        return startDate == null ? null : startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
