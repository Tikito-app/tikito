import {AssetType} from "../asset-type";

export class AggregatedHistoricalMoneyHoldingValue {
  accountIds: number[];
  date: string;
  amount: number;
  assetType: AssetType
}
