import {BudgetDateRange} from "./budget-date-range";

export default class Budget {
    id: number;
    name: string;
    dateRange: BudgetDateRange;
    amount: number;
}
