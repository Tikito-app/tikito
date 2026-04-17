export class HistoricalBudgetValue {
    id: number;
    userId: number;
    budgetId: number;
    date: string;
    budgeted: number = 0;
    spent: number = 0;
}