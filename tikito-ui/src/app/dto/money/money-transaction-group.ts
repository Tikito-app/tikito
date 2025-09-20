import MoneyTransactionGroupQualifier from "./money-transaction-group-qualifier";

export default class MoneyTransactionGroup {
    id: number;
    name: string;
    qualifiers: MoneyTransactionGroupQualifier[] = [];
}
