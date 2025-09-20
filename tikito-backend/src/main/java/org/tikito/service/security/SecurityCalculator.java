package org.tikito.service.security;

import org.tikito.dto.security.HistoricalSecurityHoldingValueDto;
import org.tikito.entity.security.AggregatedHistoricalSecurityHoldingValue;
import org.tikito.entity.security.HistoricalSecurityHoldingValue;
import org.tikito.entity.security.SecurityPrice;
import org.tikito.entity.security.SecurityTransaction;

import java.time.LocalDate;
import java.util.List;

public final class SecurityCalculator {
    private SecurityCalculator() {

    }

    /**
     * Calculates the holding value on a specific timestamp, based on the previous value of the holding, current company
     * price and a list of transactions that might be executed that day. It will first process the list of transactions,
     * and then it will calculate the value of the holding. Therefore, the value of the holding is the value of the
     * holding at the end of the day of the timestamp.
     */
    public static HistoricalSecurityHoldingValueDto calculateHistoricalValue(final LocalDate currentTimestamp,
                                                                             final HistoricalSecurityHoldingValueDto previousHoldingValue,
                                                                             final SecurityPrice securityPrice,
                                                                             final List<SecurityTransaction> transactions) {
        final HistoricalSecurityHoldingValueDto newHoldingValue = new HistoricalSecurityHoldingValueDto(currentTimestamp, previousHoldingValue);
        if (securityPrice != null) {
            newHoldingValue.setPrice(securityPrice.getPrice());
        }

        if (transactions != null) {
            transactions.forEach(transaction -> applyTransaction(newHoldingValue, transaction));
        }
        calculateWorth(newHoldingValue);

        return newHoldingValue;
    }

    /**
     * Applies a single transaction to the current value of the holding.
     */
    public static void applyTransaction(final HistoricalSecurityHoldingValueDto newHoldingValue, final SecurityTransaction transaction) {
        final double costs = transaction.getAmount() * transaction.getPrice();
        double totalMutation = 0;

        switch (transaction.getTransactionType()) {
            case BUY:
                newHoldingValue.setTotalCashInvested(newHoldingValue.getTotalCashInvested() + costs);
                totalMutation = costs;
            case BUY_PRODUCT_CHANGE:
            case BUY_ISIN_CHANGE:
                newHoldingValue.setAmount(newHoldingValue.getAmount() + transaction.getAmount());
                break;
            case SELL:
                newHoldingValue.setTotalCashWithdrawn(newHoldingValue.getTotalCashWithdrawn() + costs);
                totalMutation = costs;
            case SELL_PRODUCT_CHANGE:
            case SELL_ISIN_CHANGE:
                newHoldingValue.setAmount(newHoldingValue.getAmount() - transaction.getAmount());
                break;
            case DIVIDEND:
                newHoldingValue.setTotalDividend(newHoldingValue.getTotalDividend() + transaction.getPrice());
                totalMutation = transaction.getPrice();
                break;
            case COUNTRY_TAX:
            case TAX:
            case DIVIDEND_TAX:
                newHoldingValue.setTotalTaxes(newHoldingValue.getTotalTaxes() + transaction.getPrice());
                totalMutation = transaction.getPrice();
                break;
            case TRANSACTION_COST:
                newHoldingValue.setTotalTransactionCosts(newHoldingValue.getTotalTransactionCosts() + transaction.getPrice());
                totalMutation = transaction.getPrice();
                break;
            case ADMIN_COSTS:
            case AANSLUITKOSTEN:
                newHoldingValue.setTotalAdministrativeCosts(newHoldingValue.getTotalAdministrativeCosts() + transaction.getPrice());
                totalMutation = transaction.getPrice();
                break;
        }

        if (totalMutation < 0) {
            if (newHoldingValue.getCashOnHand() > 0) {
                final double maxAmountToSubtract = Math.min(newHoldingValue.getCashOnHand(), -totalMutation);
                newHoldingValue.setCashOnHand(newHoldingValue.getCashOnHand() - maxAmountToSubtract);
                final double remainingAmount = totalMutation + maxAmountToSubtract;
                if (remainingAmount < 0) {
                    newHoldingValue.setMaxCashInvested(newHoldingValue.getMaxCashInvested() + remainingAmount);
                }
            } else {
                newHoldingValue.setMaxCashInvested(newHoldingValue.getMaxCashInvested() + totalMutation);
            }
        } else {
            newHoldingValue.setCashOnHand(newHoldingValue.getCashOnHand() + totalMutation);
        }
    }

    /**
     * Calculate the total worth of a holding. The formula is:
     * total value of the current holding - money put into the account + dividend - external costs
     */
    public static void calculateWorth(final HistoricalSecurityHoldingValueDto holdingValue) {
        final double currentHoldingValue = holdingValue.getAmount() * holdingValue.getPrice();
        holdingValue.setWorth(calculateWorth(currentHoldingValue,
                holdingValue.getTotalCashInvested(),
                holdingValue.getTotalCashWithdrawn(),
                holdingValue.getTotalAdministrativeCosts(),
                holdingValue.getTotalTaxes(),
                holdingValue.getTotalDividend()));
    }

    /**
     * Calculate the total worth of a holding. The formula is:
     * total value of the current holding - money put into the account + dividend - external costs
     */
    public static void calculateWorth(final AggregatedHistoricalSecurityHoldingValue value) {
        value.setWorth(calculateWorth(value.getPositionValue(),
                value.getTotalCashInvested(),
                value.getTotalCashWithdrawn(),
                value.getTotalAdministrativeCosts(),
                value.getTotalTaxes(),
                value.getTotalDividend()));
    }

    public static double calculateWorth(final double currentHoldingValue,
                                        final double totalCashInvested,
                                        final double totalCashWithdrawn,
                                        final double totalAdministrativeCosts,
                                        final double totalTaxes,
                                        final double totalDividend) {
        final double worthIfWithdrawnNow = currentHoldingValue + totalCashInvested + totalCashWithdrawn;
        final double externalCosts = totalAdministrativeCosts + totalTaxes;

        return worthIfWithdrawnNow
                + totalDividend
                + externalCosts;
    }


    public static AggregatedHistoricalSecurityHoldingValue aggregateHoldingValues(final long userId, final List<HistoricalSecurityHoldingValue> historicalSecurityHoldingValues) {
        final AggregatedHistoricalSecurityHoldingValue aggregatedValue = new AggregatedHistoricalSecurityHoldingValue(userId);

        for (final HistoricalSecurityHoldingValue historicalSecurityHoldingValue : historicalSecurityHoldingValues) {

            aggregatedValue.setDate(historicalSecurityHoldingValue.getDate());
            aggregatedValue.setPositionValue(aggregatedValue.getPositionValue() + (historicalSecurityHoldingValue.getAmount() * historicalSecurityHoldingValue.getPrice()));
            aggregatedValue.setTotalAdministrativeCosts(aggregatedValue.getTotalAdministrativeCosts() + historicalSecurityHoldingValue.getTotalAdministrativeCosts());
            aggregatedValue.setTotalCashInvested(aggregatedValue.getTotalCashInvested() + historicalSecurityHoldingValue.getTotalCashInvested());
            aggregatedValue.setTotalTaxes(aggregatedValue.getTotalTaxes() + historicalSecurityHoldingValue.getTotalTaxes());
            aggregatedValue.setTotalDividend(aggregatedValue.getTotalDividend() + historicalSecurityHoldingValue.getTotalDividend());
            aggregatedValue.setTotalCashWithdrawn(aggregatedValue.getTotalCashWithdrawn() + historicalSecurityHoldingValue.getTotalCashWithdrawn());
            aggregatedValue.setTotalTransactionCosts(aggregatedValue.getTotalTransactionCosts() + historicalSecurityHoldingValue.getTotalTransactionCosts());
            aggregatedValue.setMaxCashInvested(aggregatedValue.getMaxCashInvested() + historicalSecurityHoldingValue.getMaxCashInvested());
            aggregatedValue.setCashOnHand(aggregatedValue.getCashOnHand() + historicalSecurityHoldingValue.getCashOnHand());
        }
        SecurityCalculator.calculateWorth(aggregatedValue);

        return aggregatedValue;
    }
}
