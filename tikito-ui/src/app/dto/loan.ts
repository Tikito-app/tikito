import {LoanPart} from "./loan-part";
import MoneyTransactionGroup from "./money/money-transaction-group";
import {BudgetDateRange} from "./budget/budget-date-range";

export class Loan {
  id: number;
  name: string;
  loanParts: LoanPart[] = [];
  dateRange: BudgetDateRange;
  groups: MoneyTransactionGroup[] = [];
}
