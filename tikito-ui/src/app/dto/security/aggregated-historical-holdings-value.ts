export default class AggregatedHistoricalHoldingsValue {
  id: number;
  accountIds: number[];
  date: string;
  positionValue: number;
  totalDividend: number;
  totalAdministrativeCosts: number;
  totalTaxes: number;
  totalTransactionCosts: number;
  totalCashInvested: number;
  totalCashWithdrawn: number;
  worth: number;
  cashOnHand: number = 0;
  maxCashInvested: number = 0;
}
