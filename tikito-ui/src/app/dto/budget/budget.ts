import {BudgetDateRange} from "./budget-date-range";
import MoneyTransactionGroup from "../money/money-transaction-group";

export default class Budget {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
    dateRange: BudgetDateRange;
    dateRangeAmount: number;
    amount: number;
    groups: MoneyTransactionGroup[];
}
