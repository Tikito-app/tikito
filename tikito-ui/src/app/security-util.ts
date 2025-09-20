import {SecurityHoldingValue} from "./dto/security/security-holding-value";
import AggregatedHistoricalHoldingsValue from "./dto/security/aggregated-historical-holdings-value";
import HistoricalHoldingValue from "./dto/security/historical-holding-value";

export class SecurityUtil {
  static getPerformance(holdingValue: SecurityHoldingValue): number {
    let initialCosts = -holdingValue.maxCashInvested;
    let currentValue = this.getPositionValue(holdingValue) + holdingValue.cashOnHand;
    let v = currentValue - initialCosts;
    return v / initialCosts * 100;

  }
  static getPerformanceBetween(holdingValue: HistoricalHoldingValue, previousHoldingValue: HistoricalHoldingValue): number {
    let initialCosts = holdingValue.totalCashInvested + holdingValue.totalAdministrativeCosts + holdingValue.totalTaxes;
    let previousInitialCosts = previousHoldingValue.totalCashInvested + previousHoldingValue.totalAdministrativeCosts + previousHoldingValue.totalTaxes;
    let currentValue = this.getTotalProfit(holdingValue);
    let previousValue = this.getTotalProfit(previousHoldingValue);
    let startTotalValue = previousValue - previousInitialCosts;
    let currentTotalValue = currentValue - initialCosts;
    return (currentTotalValue - startTotalValue) / startTotalValue;
  }

  static getPerformanceAggregated(holdingValue: AggregatedHistoricalHoldingsValue): number {
    let initialCosts = -holdingValue.maxCashInvested;
    let currentValue = holdingValue.positionValue + holdingValue.cashOnHand;
    let v = currentValue - initialCosts;
    return v / initialCosts * 100;
  }

  /**
   * Returns the total value of this holding of it would be sold now
   * @param holdingValue
   */
  static getTotalValue(holdingValue: SecurityHoldingValue): number {
    return this.getTotalProfit(holdingValue) + this.getTotalCosts(holdingValue);
  }

  /**
   * Returns the total profit of this holding if it would be sold now
   * @param holdingValue
   */
  static getTotalProfit(holdingValue: SecurityHoldingValue): number {
    return this.getPositionValue(holdingValue)
      + holdingValue.totalCashWithdrawn
      + holdingValue.totalDividend
  }

  static getTotalProfitAggregated(holdingValue: AggregatedHistoricalHoldingsValue): number {
    return holdingValue.positionValue
      // + holdingValue.totalCashWithdrawn
      + holdingValue.totalDividend

  }

  /**
   * Returns the costs of this holding if it would be withdrawn now
   * @param holdingValue
   */
  static getTotalCosts(holdingValue: SecurityHoldingValue): number {
    return holdingValue.totalCashInvested
      + holdingValue.totalTaxes
      + holdingValue.totalTransactionCosts;
  }

  /**
   * The value of the current position (without any dividend, taxes etc.)
   */
  static getPositionValue(holdingValue: SecurityHoldingValue): number {
    return holdingValue.price * holdingValue.amount;
  }
}
