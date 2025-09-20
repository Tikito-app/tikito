import {Security} from "./security";

export class SecurityTransaction {
  id: number;
  securityId: number;
  isin: string;
  accountId: number;
  currency: string;
  amount: number;
  price: number;
  description: string;
  timestamp: string;
  transactionType: string; // todo
  security: Security;
  exchangeRate: number;
}
