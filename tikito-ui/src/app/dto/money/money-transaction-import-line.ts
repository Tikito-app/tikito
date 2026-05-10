import {DebitCredit} from "../debit-credit";
import {MT940Transaction} from "./mt940-transaction";

export class MoneyTransactionImportLine {
  counterpartyAccountNumber: string;
  counterpartyAccountName: string;
  timestamp: string;
  debitCredit: DebitCredit;
  code: string;
  amount: number;
  finalBalance: number;
  currencyId: number;
  transactionType: string;
  description: string;
  lineNumber: number;
  cells: string[];
  mt940Transaction: MT940Transaction;
  failed: boolean;
  failedReason: string;
  exchangeRate: number;
}
