import {BudgetDateRange} from "./budget-date-range";

export default class Budget {
    id: number;
    startDate: string;
    endDate: string;
    dateRange: BudgetDateRange;
    dateRangeAmount: number;
    amount: number;

    // ui only
    spent: number;
}
