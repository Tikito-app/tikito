import MoneyTransactionGroupQualifier from "./money-transaction-group-qualifier";
import {MoneyTransactionGroupType} from "../money-transaction-group-type";
import {MoneyDateRange} from "./money-date-range";

export default class MoneyTransactionGroup {
    id: number;
    name: string;
    groupTypes: MoneyTransactionGroupType[];
    qualifiers: MoneyTransactionGroupQualifier[] = [];
    accountIds: number[] = [];
    startDate: string;
    endDate: string;
    dateRange: MoneyDateRange;
    dateRangeAmount: number;
    budgeted: number;
}
