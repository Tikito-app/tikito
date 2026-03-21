import {Account} from "./account";

export class MoneyHolding {
  id: number;
  accountId: number;
  currencyId: number;
  amount: number;
  amountOffset: number;
  account: Account;
}