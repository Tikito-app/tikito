import MoneyTransactionGroupQualifier from "./money-transaction-group-qualifier";
import {MoneyTransactionGroupType} from "../money-transaction-group-type";

export default class MoneyTransactionGroup {
    id: number;
    name: string;
    groupTypes: MoneyTransactionGroupType[];
    qualifiers: MoneyTransactionGroupQualifier[] = [];
    accountIds: number[] = [];
}
