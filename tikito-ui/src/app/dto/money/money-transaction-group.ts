import MoneyTransactionGroupQualifier from "./money-transaction-group-qualifier";
import {MoneyTransactionGroupType} from "../money-transaction-group-type";
import Budget from "../budget/budget";
import {BudgetDateRange} from "../budget/budget-date-range";

export default class MoneyTransactionGroup {
    id: number;
    name: string;
    groupTypes: MoneyTransactionGroupType[];
    qualifiers: MoneyTransactionGroupQualifier[] = [];
    accountIds: number[] = [];
    startDate: string;
    endDate: string;
    dateRange: BudgetDateRange;
    dateRangeAmount: number;
    amount: number;
}
