export default class MoneyTransaction {
  id: number;
  accountId: number;
  counterpartAccountName: string;
  counterpartAccountNumber: string;
  timestamp: string;
  amount: number;
  finalBalance: number;
  description: string;
  currencyId: number;
  groupId: number;
  groupName: string;
  budgetId: number;
  loanId: number;
  exchangeRate: number;
}
