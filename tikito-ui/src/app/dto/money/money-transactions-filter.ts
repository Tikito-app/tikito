import moment, {Moment} from "moment/moment";

export class MoneyTransactionsFilter {
  accountIds: number[] | null;
  currencies: number[] | null;
  groupIds: number[] | null;
  dateRange: TransactionDateRange | null;
  startAtZeroAfterDateAggregation: boolean;
  startAtZeroFromBeginning: boolean;
  aggregateDateRange: boolean;
  nonGrouped: boolean;
  startDate: string | null;
  endDate: string | null;
  amountOfOtherGroups: number;
  includeBudget: boolean;
  includeMoney: boolean;
  includeMoneyHolding: boolean;
  transactionFilter: string;

  withoutStartAndEndDate(): MoneyTransactionsFilter {
    let filter = new MoneyTransactionsFilter();
    filter.accountIds = this.accountIds;
    filter.currencies = this.currencies;
    filter.groupIds = this.groupIds;
    filter.startAtZeroAfterDateAggregation = this.startAtZeroAfterDateAggregation;
    filter.startAtZeroFromBeginning = this.startAtZeroFromBeginning;
    filter.aggregateDateRange = this.aggregateDateRange;
    filter.nonGrouped = this.nonGrouped;
    filter.amountOfOtherGroups = this.amountOfOtherGroups;
    filter.includeBudget = this.includeBudget;
    filter.includeMoney = this.includeMoney;
    filter.includeMoneyHolding = this.includeMoneyHolding;
    filter.dateRange = this.dateRange;
    filter.transactionFilter = this.transactionFilter;
    return filter;
  }

  withoutStartDate() {
    let filter = this.withoutStartAndEndDate();
    filter.endDate = this.endDate;
    return filter;
  }

  getStartDate(): Moment {
    return (this.startDate == null ? null : moment(this.startDate)) as Moment;
  }

  getEndDate(): Moment {
    return (this.endDate == null ? null : moment(this.endDate)) as Moment;
  }
}

export enum TransactionDateRange {
  YEAR = 'YEAR',
  MONTH = 'MONTH',
  WEEK = 'WEEK',
  ALL = 'ALL'
}
