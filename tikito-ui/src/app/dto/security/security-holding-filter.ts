import {TransactionDateRange} from "../money/money-transactions-filter";
import {SecurityHoldingGraphDisplayField} from "./security-holding-graph-display-field";

export class SecurityHoldingFilter {
  holdingIds: number[] | null;
  dateRange: TransactionDateRange | null;
  startDate: string;
  displayField: SecurityHoldingGraphDisplayField;
  startAtZeroAfterDateAggregation: boolean;
  startAtZeroFromBeginning: boolean;
}
