import {SecurityTransactionType} from "./security-transaction-type";

export class SecurityTransactionImportLine {
  timestamp: string;
  isin: string;
  productName: string;
  currency: string;
  currencyId: number;
  amount: number;
  price: number;
  description: string;
  transactionType: SecurityTransactionType;
  country: string;
  cash: number;
  securityId: number;
  lineNumber: number;
  cells: string[];
  failed: boolean;
  failedReason: string;
  exchangeRate: number;
}
