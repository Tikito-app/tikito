import {AccountType} from "./account-type";

export class Account {
  id: number;
  name: string;
  accountNumber: string;
  accountType: AccountType;
  currencyId: number;
}
