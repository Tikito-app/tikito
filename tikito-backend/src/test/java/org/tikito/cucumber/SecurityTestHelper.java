package org.tikito.cucumber;

import org.tikito.dto.security.AggregatedHistoricalSecurityHoldingValueDto;
import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;

/// This class is copied from the front end
public final class SecurityTestHelper {

    public static double getPerformance(final HistoricalSecurityHoldingValueDto holdingValue) {
        final double initialCosts = -holdingValue.getMaxCashInvested();
        final double currentValue = getPositionValue(holdingValue) + holdingValue.getCashOnHand();
        final double v = currentValue - initialCosts;
        return v / initialCosts * 100;

    }
    private static double getPerformanceBetween(final HistoricalSecurityHoldingValueDto holdingValue, final HistoricalSecurityHoldingValueDto previousHoldingValue) {
        final double initialCosts = holdingValue.getTotalCashInvested() + holdingValue.getTotalAdministrativeCosts() + holdingValue.getTotalTaxes();
        final double previousInitialCosts = previousHoldingValue.getTotalCashInvested() + previousHoldingValue.getTotalAdministrativeCosts() + previousHoldingValue.getTotalTaxes();
        final double currentValue = getTotalProfit(holdingValue);
        final double previousValue = getTotalProfit(previousHoldingValue);
        final double startTotalValue = previousValue - previousInitialCosts;
        final double currentTotalValue = currentValue - initialCosts;
        return (currentTotalValue - startTotalValue) / startTotalValue;
    }

    private static double getPerformanceAggregated(final AggregatedHistoricalSecurityHoldingValueDto holdingValue) {
        final double initialCosts = -holdingValue.getMaxCashInvested();
        final double currentValue = holdingValue.getPositionValue() + holdingValue.getCashOnHand();
        final double v = currentValue - initialCosts;
        return v / initialCosts * 100;
    }

    /**
     * Returns the total value of this holding of it would be sold now
     * @param holdingValue
     */
    private static double getTotalValue(final HistoricalSecurityHoldingValueDto holdingValue) {
        return getTotalProfit(holdingValue) + getTotalCosts(holdingValue);
    }

    /**
     * Returns the total profit of this holding if it would be sold now
     * @param holdingValue
     */
    private static double getTotalProfit(final HistoricalSecurityHoldingValueDto holdingValue) {
        return getPositionValue(holdingValue)
                + holdingValue.getTotalCashWithdrawn()
                + holdingValue.getTotalDividend();
    }

    private static double getTotalProfitAggregated(final AggregatedHistoricalSecurityHoldingValueDto holdingValue) {
        return holdingValue.getPositionValue()
                // + holdingValue.totalCashWithdrawn
                + holdingValue.getTotalDividend();

    }

    /**
     * Returns the costs of this holding if it would be withdrawn now
     * @param holdingValue
     */
    private static double getTotalCosts(final HistoricalSecurityHoldingValueDto holdingValue) {
        return holdingValue.getTotalCashInvested()
                + holdingValue.getTotalTaxes()
                + holdingValue.getTotalTransactionCosts();
    }

    /**
     * The value of the current position (without any dividend, taxes etc.)
     */
    private static double getPositionValue(final HistoricalSecurityHoldingValueDto holdingValue) {
        return holdingValue.getPrice() * holdingValue.getAmount();
    }
}
