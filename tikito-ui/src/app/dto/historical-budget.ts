import {BudgetDateRange} from "./budget/budget-date-range";

export class HistoricalBudget {
    id: number;
    name: string;
    dateRange: BudgetDateRange;
    amount: number;
}
