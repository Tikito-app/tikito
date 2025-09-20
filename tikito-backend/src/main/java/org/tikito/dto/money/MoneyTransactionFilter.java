package org.tikito.dto.money;

import org.tikito.dto.DateRange;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MoneyTransactionFilter {
    private Set<Long> accountIds;
    private Set<Long> groupIds;
    private DateRange dateRange;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean nonGrouped;
    private boolean aggregateDateRange;

    public Set<Long> getAccountIds() {
        if (accountIds != null) {
            final Set<Long> ids = accountIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            if (!ids.isEmpty()) {
                return ids;
            }
        }
        return null;
    }

    public DateRange getDateRange() {
        if (dateRange == null) {
            return DateRange.ALL;
        }
        return dateRange;
    }

    public Set<Long> getGroupIds() {
        if (groupIds != null) {
            final Set<Long> ids = groupIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
            if (!ids.isEmpty()) {
                return ids;
            }
        }
        return null;
    }

    public Instant getStartDateAsInstant() {
        return startDate == null ? null : startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public Instant getEndDateAsInstant() {
        return endDate == null ? null : endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
