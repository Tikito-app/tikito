import {MoneyType} from "./money-type";

export class AggregatedHistoricalMoneyHoldingValue {
  accountIds: number[];
  date: string;
  amount: number;
  moneyType: MoneyType
}
