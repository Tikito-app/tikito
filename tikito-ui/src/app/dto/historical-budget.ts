import {MoneyDateRange} from "./money/money-date-range";

export class HistoricalBudget {
    id: number;
    name: string;
    dateRange: MoneyDateRange;
    amount: number;
}
