export class MoneyTransactionsFilter {
  accountIds: number[] | null;
  groupIds: number[] | null;
  dateRange: TransactionDateRange | null;
  startAtZeroAfterDateAggregation: boolean;
  startAtZeroFromBeginning: boolean;
  aggregateDateRange: boolean;
  nonGrouped: boolean;
  startDate: string;
  endDate: string;
  showOther: boolean;
  amountOfOtherGroups: number;

  withoutStartAndEndDate(): MoneyTransactionsFilter {
    let filter = new MoneyTransactionsFilter();
    filter.accountIds = this.accountIds;
    filter.groupIds = this.groupIds;
    filter.startAtZeroAfterDateAggregation = this.startAtZeroAfterDateAggregation;
    filter.startAtZeroFromBeginning = this.startAtZeroFromBeginning;
    filter.aggregateDateRange = this.aggregateDateRange;
    filter.nonGrouped = this.nonGrouped;
    filter.showOther = this.showOther;
    filter.amountOfOtherGroups = this.amountOfOtherGroups;
    return filter;
  }
}

export enum TransactionDateRange {
  YEAR = 'YEAR',
  MONTH = 'MONTH',
  WEEK = 'WEEK',
  ALL = 'ALL'
}
