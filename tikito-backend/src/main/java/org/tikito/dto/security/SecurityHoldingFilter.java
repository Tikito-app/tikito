package org.tikito.dto.security;

import org.tikito.dto.DateRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class SecurityHoldingFilter {
    private Set<Long> holdingIds;
    private DateRange dateRange;
    private LocalDate startDate;

    public Set<Long> getHoldingIds() {
        if (holdingIds != null && !holdingIds.isEmpty()) {
            return holdingIds;
        }
        return null;
    }

    public Instant getStartDateAsInstant() {
        return startDate == null ? null : startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
