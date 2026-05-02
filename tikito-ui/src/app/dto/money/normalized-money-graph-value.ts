import moment from "moment/moment";

export class NormalizedMoneyGraphValue {
  dateString: string;
  date: moment.Moment;
  value: number;
  groupKey: string;
  currencyId: number;
  previous: NormalizedMoneyGraphValue | null;

  constructor(dateString: string, date: moment.Moment, value: number, groupKey: string, currencyId: number, previous: NormalizedMoneyGraphValue | null) {
    this.dateString = dateString;
    this.date = date;
    this.value = value;
    this.groupKey = groupKey;
    this.currencyId = currencyId;
    this.previous = previous;
  }
}