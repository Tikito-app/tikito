package org.tikito.service;

import org.tikito.dto.OverviewDto;
import org.tikito.entity.security.AggregatedHistoricalSecurityHoldingValue;
import org.tikito.repository.AggregatedHistoricalSecurityHoldingValueRepository;
import org.springframework.stereotype.Service;

@Service
public class OverviewService {
    private final AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository;

    public OverviewService(final AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository) {
        this.aggregatedHistoricalSecurityHoldingValueRepository = aggregatedHistoricalSecurityHoldingValueRepository;
    }

    public OverviewDto getOverview(final long userId) {
        final OverviewDto overviewDto = new OverviewDto();

        aggregatedHistoricalSecurityHoldingValueRepository
                .findLatestByUserId(userId)
                .ifPresent(value -> apply(overviewDto, value));

        return overviewDto;
    }

    private static void apply(final OverviewDto overviewDto, final AggregatedHistoricalSecurityHoldingValue value) {
        overviewDto.setPositionValue(value.getPositionValue());
        overviewDto.setTotalDividend(value.getTotalDividend());
        overviewDto.setTotalAdministrativeCosts(value.getTotalAdministrativeCosts());
        overviewDto.setTotalTaxes(value.getTotalTaxes());
        overviewDto.setTotalTransactionCosts(value.getTotalTransactionCosts());
        overviewDto.setTotalCashInvested(value.getTotalCashInvested());
        overviewDto.setTotalCashWithdrawn(value.getTotalCashWithdrawn());
        overviewDto.setWorth(value.getWorth());
        overviewDto.setMaxCashInvested(value.getMaxCashInvested());
        overviewDto.setCashOnHand(value.getCashOnHand());
    }
}
