export class LoanValue {
  id: number;
  loanId: number;
  loanPartId: number;
  date: string;
  amountRemaining: number;
  interestPaid: number;
  interestPaidThisPeriod: number;
  interestRemaining: number;
  repaymentRemaining: number;
  periodicPayment: number;
  loanPaid: number;
  loanPaidThisPeriod: number;
  simulated: boolean;
}
