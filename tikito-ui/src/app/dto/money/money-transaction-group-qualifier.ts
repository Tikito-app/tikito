import {MoneyTransactionGroupQualifierType} from "./money-transaction-group-qualifier-type";
import {MoneyTransactionField} from "./money-transaction-field";

export default class MoneyTransactionGroupQualifier {
    id: number;
    accountId: number;
    qualifierType: MoneyTransactionGroupQualifierType;
    qualifier: string;
    transactionField: MoneyTransactionField;
}
