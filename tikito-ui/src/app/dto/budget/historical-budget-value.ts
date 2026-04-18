export class HistoricalBudgetValue {
    id: number;
    userId: number;
    groupId: number;
    date: string;
    budgeted: number = 0;
    spent: number = 0;
}