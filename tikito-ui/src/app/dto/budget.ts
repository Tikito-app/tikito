import {BudgetDateRange} from "./budget-date-range";
import MoneyTransactionGroup from "./money/money-transaction-group";

export default class Budget {
    id: number;
    name: string;
    dateRange: BudgetDateRange;
    amount: number;
    groups: MoneyTransactionGroup[];
}
