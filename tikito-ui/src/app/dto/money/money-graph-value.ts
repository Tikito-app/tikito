import moment from "moment/moment";

export class MoneyGraphValue {
  dateRangeString: string;
  date: moment.Moment;
  value: number;
  groupKey: string;
  currencyId: number;
  previous: MoneyGraphValue | null;

  constructor(dateRangeString: string, date: moment.Moment, value: number, groupKey: string, currencyId: number, previous: MoneyGraphValue | null) {
    this.dateRangeString = dateRangeString;
    this.date = date;
    this.value = value;
    this.groupKey = groupKey;
    this.currencyId = currencyId;
    this.previous = previous;
  }
}