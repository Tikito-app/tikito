import {MoneyGraphGroupKey} from "./money-graph-group-key";

export class MoneyGraphGroupInfo {
  id: number
  name: string;
  isBudget: boolean;
  isHolding: boolean;
  key: string;
  normalizedAggregatedValue: number = 0;
  currencyId: number;

  constructor(id: number, name: string, isBudget: boolean, isHolding: boolean, currencyId: number) {
    this.id = id;
    this.name = name;
    this.isBudget = isBudget;
    this.isHolding = isHolding;
    this.key = new MoneyGraphGroupKey(name, isBudget, isHolding).toString();
    this.currencyId = currencyId;
  }
}