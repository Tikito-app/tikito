import {LoanPart} from "./loan-part";
import MoneyTransactionGroup from "./money/money-transaction-group";
import {MoneyDateRange} from "./money/money-date-range";

export class Loan {
  id: number;
  name: string;
  loanParts: LoanPart[] = [];
  dateRange: MoneyDateRange;
  groups: MoneyTransactionGroup[] = [];
}
