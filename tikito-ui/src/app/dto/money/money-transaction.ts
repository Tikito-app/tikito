export default class MoneyTransaction {
  id: number;
  accountId: number;
  counterpartAccountName: string;
  counterpartAccountNumber: string;
  timestamp: string;
  amount: number;
  finalBalance: number;
  description: string;
  currency: string;
  groupId: number;
  groupName: string;
  exchangeRate: number;
}
