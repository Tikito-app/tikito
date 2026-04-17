import MoneyTransaction from "./money-transaction";

export class MoneyBudgetTransaction extends MoneyTransaction {
  budgeted: number;
  spent: number;
  groupIds: number[]; // money group ids
}