import {LoanPart} from "./loan-part";
import MoneyTransactionGroup from "./money/money-transaction-group";

export class Loan {
  id: number;
  name: string;
  loanParts: LoanPart[] = [];
  groups: MoneyTransactionGroup[] = [];
}
