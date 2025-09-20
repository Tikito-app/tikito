import {SecurityHoldingValue} from "./security-holding-value";

export default class HistoricalHoldingValue extends SecurityHoldingValue {
  id: number;
  accountIds: number[];
  securityHoldingId: number;
  securityId: number;
  isin: string;
  date: string;
}
