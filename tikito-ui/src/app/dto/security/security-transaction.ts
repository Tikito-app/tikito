import {Security} from "./security";
import {SecurityTransactionType} from "./security-transaction-type";

export class SecurityTransaction {
  id: number;
  securityId: number;
  isin: string;
  accountId: number;
  currencyId: number;
  amount: number;
  price: number;
  description: string;
  timestamp: string;
  transactionType: SecurityTransactionType; // todo
  security: Security;
  exchangeRate: number;
  cash: number;
}
