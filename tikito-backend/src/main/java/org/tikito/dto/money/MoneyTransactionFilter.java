package org.tikito.dto.money;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.DateRange;
import org.tikito.util.Util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class MoneyTransactionFilter {
    private Set<Long> accountIds;
    private Set<Long> currencies;
    private Set<Long> groupIds;
    private DateRange dateRange;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean nonGrouped;
    private Boolean aggregateDateRange;
    private String transactionFilter;

    public Set<Long> getAccountIds() {
        return Util.toSetOfNonNullLongs(accountIds);
    }

    public Set<Long> getCurrencies() {
        return Util.toSetOfNonNullLongs(currencies);
    }

    public DateRange getDateRange() {
        if (dateRange == null) {
            return DateRange.ALL;
        }
        return dateRange;
    }

    public Set<Long> getGroupIds() {
        return Util.toSetOfNonNullLongs(groupIds);
    }

    public Instant getStartDateAsInstant() {
        return startDate == null ? null : startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public Instant getEndDateAsInstant() {
        return endDate == null ? null : endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
