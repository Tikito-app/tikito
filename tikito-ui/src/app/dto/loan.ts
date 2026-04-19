import {LoanPart} from "./loan-part";
import MoneyTransactionGroup from "./money/money-transaction-group";
import {DateRange} from "./date-range";

export class Loan {
  id: number;
  name: string;
  loanParts: LoanPart[] = [];
  dateRange: DateRange;
  groups: MoneyTransactionGroup[] = [];
}
