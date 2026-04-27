export class MoneyTransactionsFilter {
  accountIds: number[] | null;
  currencies: number[] | null;
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
  includeBudget: boolean;
  includeMoney: boolean;
  includeMoneyHolding: boolean;

  withoutStartAndEndDate(): MoneyTransactionsFilter {
    let filter = new MoneyTransactionsFilter();
    filter.accountIds = this.accountIds;
    filter.currencies = this.currencies;
    filter.groupIds = this.groupIds;
    filter.startAtZeroAfterDateAggregation = this.startAtZeroAfterDateAggregation;
    filter.startAtZeroFromBeginning = this.startAtZeroFromBeginning;
    filter.aggregateDateRange = this.aggregateDateRange;
    filter.nonGrouped = this.nonGrouped;
    filter.showOther = this.showOther;
    filter.amountOfOtherGroups = this.amountOfOtherGroups;
    filter.includeBudget = this.includeBudget;
    filter.includeMoney = this.includeMoney;
    filter.includeMoneyHolding = this.includeMoneyHolding;
    filter.dateRange = this.dateRange;
    return filter;
  }
}

export enum TransactionDateRange {
  YEAR = 'YEAR',
  MONTH = 'MONTH',
  WEEK = 'WEEK',
  ALL = 'ALL'
}
