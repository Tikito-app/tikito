import {LoanType} from "./loan-type";
import {LoanInterest} from "./loan-interest";

export class LoanPart {
  id: number;
  userId: number;
  loanId: number;
  name: string;
  startDate: string;
  endDate: string;
  amount: number;
  remainingAmount: number;
  currencyId: number;
  loanType: LoanType;
  interests: LoanInterest[];
}
