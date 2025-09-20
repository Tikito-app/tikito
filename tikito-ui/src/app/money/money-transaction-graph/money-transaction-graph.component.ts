import {Component, EventEmitter, Input, OnInit} from '@angular/core';
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import * as echarts from 'echarts/core';
import {EChartModule} from "../../echart-module";
import {MoneyApi} from "../../api/money-api";
import {Util} from "../../util";
import {MoneyTransactionsFilter, TransactionDateRange} from "../../dto/money/money-transactions-filter";
import moment from "moment";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import MoneyTransaction from "../../dto/money/money-transaction";
import {Observable} from "rxjs";
import {AuthService} from "../../service/auth.service";


class GroupInfo {
  id: number
  name: string;
  normalizedAggregatedValue: number = 0;

  constructor(id: number, name: string) {
    this.id = id;
    this.name = name;
  }
}

export class NormalizedMoneyValue {
  dateString: string;
  date: moment.Moment;
  value: number;
  groupName: string;
  previous: NormalizedMoneyValue | null;

  constructor(dateString: string, date: moment.Moment, value: number, groupName: string, previous: NormalizedMoneyValue | null) {
    this.dateString = dateString;
    this.date = date;
    this.value = value;
    this.groupName = groupName;
    this.previous = previous;
  }
}


@Component({
  selector: 'app-money-transaction-graph',
  standalone: true,
  imports: [EChartModule, NgxEchartsDirective],
  templateUrl: './money-transaction-graph.component.html',
  styleUrl: './money-transaction-graph.component.scss',
  providers: [
    provideEchartsCore({echarts}),
  ]
})
export class MoneyTransactionGraphComponent implements OnInit {
  @Input()
  accountId: number;

  @Input()
  height: number;

  allTransactions: MoneyTransaction[];
  normalizedValues: NormalizedMoneyValue[];
  aggregatedValuesPerDateRange: NormalizedMoneyValue[];
  groupsByName: any;
  highestValuedGroups: any;
  groupsById: any;
  moneyTransactionGroups: MoneyTransactionGroup[] = [];

  @Input()
  transactionFilter: MoneyTransactionsFilter;

  @Input() onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter>;

  chartOption: any;

  performanceTimes: any = {};
  lastPerformanceName: string;

  constructor(private api: MoneyApi,
              private authService: AuthService) {
  }

  perf(name: string) {
    let now = Date.now();
    this.performanceTimes[name] = now;

    if (this.lastPerformanceName != null) {
      console.log(this.lastPerformanceName + ' took ' + (now - this.performanceTimes[this.lastPerformanceName]));
    }

    this.lastPerformanceName = name;
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.onFilterUpdateCallback.subscribe(filter => {
        this.transactionFilter = filter;
        this.resetGraph();
      });
      this.api.getMoneyTransactionGroups().subscribe(groups => {
        this.moneyTransactionGroups = groups;
      });
    });
  }

  accountsOfTransactions: any;

  assertHasTransactions(): Observable<void> {
    this.perf('getTransactions');
    if(this.accountsOfTransactions != this.transactionFilter.accountIds) {
      this.allTransactions = [];
    }
    this.accountsOfTransactions = this.transactionFilter.accountIds;
    return new Observable(observer => {
      if (this.allTransactions != null && this.allTransactions.length > 0) {
        observer.next();
      } else {
        this.api.getTransactions(this.transactionFilter.withoutStartAndEndDate()).subscribe(transactions => {
          this.allTransactions = transactions;
          if (this.allTransactions != null && this.allTransactions.length > 0) {
            observer.next();
          }
        });
      }
    });
  }

  resetGraph() {
    this.assertHasTransactions().subscribe(() => {
      this.perf('save');

      this.perf('generateGroupsByName');
      this.generateGroupsByName();
      this.perf('mapHistoricalMoneyValueToNormalizedMoneyValue');
      this.mapHistoricalMoneyValueToNormalizedMoneyValue();
      this.perf('calculateNormalizedAggregatedValues');
      this.calculateNormalizedAggregatedValues();
      this.perf('splitGroupsAndMapByName');
      this.splitGroupsAndMapByName();
      this.perf('aggregateValues');
      this.aggregateValues();
      this.perf('generateGraphOptions');
      this.generateGraph();
      this.perf('end');
    });
  }


  /**
   * Generate group names:
   * groupsByName['name'] = {id: string, name: string}
   */
  generateGroupsByName() {
    // first the defined groups
    this.groupsByName = {};
    this.groupsById = {};
    this.moneyTransactionGroups.forEach(group => {
      this.groupsByName[group.name] = new GroupInfo(group.id, group.name);
      this.groupsById[group.id] = group;
    });

    // now put all the non-grouped values in it by the counterparty name
    this
      .allTransactions
      .filter(value => value.groupId == null)
      .forEach(value => this.groupsByName[this.getGroupNameOfHistoricalValue(value)] = new GroupInfo(-1, this.getGroupNameOfHistoricalValue(value)));
  }

  /**
   * normalizedValues = [
   *   dateString: string;
   *   date: moment.Moment;
   *   value: number;
   *   groupName: string;
   * ];
   *
   * This variable contains all groups
   */
  mapHistoricalMoneyValueToNormalizedMoneyValue() {
    let format = this.getDateRangeFormat();
    this.normalizedValues = this.allTransactions
      .map(value => {
        let date = moment(value.timestamp);
        let dateRangeString = date.format(format); // format
        let dateRange = moment(dateRangeString, format); // and back to moment for start of the date range

        return new NormalizedMoneyValue(
          dateRangeString,
          dateRange,
          value.amount,
          this.getGroupNameOfHistoricalValue(value),
          null);
      });
  }

  /**
   * Sums up the positive value for each grouped and non-grouped value in the groupsByName.
   */
  calculateNormalizedAggregatedValues() {
    let startDate = this.getStartDate();
    let endDate = this.getEndDate();
    this
      .normalizedValues
      // we have to filter here, in order to already calculate the transaction that we are interested in
      .filter(value => startDate == null || startDate.isBefore(value.date))
      .filter(value => endDate == null || endDate.isSameOrAfter(value.date))
      .forEach(value => {
        this.groupsByName[value.groupName].normalizedAggregatedValue += this.getPositiveValue(value.value);
      });
  }

  /**
   * Split the groupsByName into the highest valued non-grouped values. Then, add the
   * grouped values as well.
   */
  splitGroupsAndMapByName() {
    this.highestValuedGroups = {};

    // first limit to all non-grouped groups and then sort, because groupsByName contains both
    // grouped and non-grouped values.
    // We don't filter on date here, because that is already done in calculateNormalizedAggregatedValues()
    Object.values(this.groupsByName)
      .filter((group: any) => group.id == -1)
      .sort((group1: any, group2: any) => {
        if (group1.normalizedAggregatedValue < group2.normalizedAggregatedValue) {
          return 1;
        } else if (group1.normalizedAggregatedValue > group2.normalizedAggregatedValue) {
          return -1;
        }
        return 0;
      })
      .slice(0, this.transactionFilter.amountOfOtherGroups)
      .forEach((group: any) => this.highestValuedGroups[group.name] = group);

    // now add the user defined groups
    Object.values(this.groupsByName)
      .filter((group: any) => group.id != -1)
      .forEach((group: any) => {
        this.highestValuedGroups[group.name] = group;
      });
  }

  /**
   * Because we are dealing with transactions, it will happen that we have multiple
   * values per date range value. We must aggregate the values of those, in order to have a
   * single value per date range value. We also must sort by oldest first.
   */
  aggregateValues() {
    let valuesPerGroupAndDateRange: any = {};
    let previousValuesPerGroup: any = {};

    this.normalizedValues
      // .filter(value => !this.transactionFilter.showOther && this.highestValuedGroups[value.groupName] == null)
      .forEach(value => {
        let isLowestGroupValue = this.highestValuedGroups[value.groupName] == null;
        if (isLowestGroupValue) {
          value.groupName = 'Other';
        }

        let dateString = value.dateString;
        if (valuesPerGroupAndDateRange[value.groupName] == null) {
          valuesPerGroupAndDateRange[value.groupName] = {};
        }
        if (valuesPerGroupAndDateRange[value.groupName][dateString] == null) {
          // we are not interested in the sum of transaction values on that date range, but we want to
          // know total accumulated amount over all time
          let startValue = previousValuesPerGroup[value.groupName] == null ? 0 : previousValuesPerGroup[value.groupName].value;
          valuesPerGroupAndDateRange[value.groupName][dateString] = new NormalizedMoneyValue(
            dateString,
            value.date,
            startValue,
            value.groupName,
            previousValuesPerGroup[value.groupName]);
          previousValuesPerGroup[value.groupName] = valuesPerGroupAndDateRange[value.groupName][dateString];
        }
        valuesPerGroupAndDateRange[value.groupName][dateString].value += value.value;
      });

    this.aggregatedValuesPerDateRange = [];
    Object
      .values(valuesPerGroupAndDateRange)
      .forEach((o: any) =>
        Object
          .values(o)
          .forEach((value: any) => this.aggregatedValuesPerDateRange.push(value)));

    this.aggregatedValuesPerDateRange.sort((value1, value2) => {
      if (value1.date.isAfter(value2.date)) {
        return 1;
      } else if (value2.date.isAfter(value1.date)) {
        return -1;
      }
      return 0;
    });

  }


  generateGraph() {
    let groupNames = Object.keys(this.highestValuedGroups);
    if (this.transactionFilter.showOther) {
      groupNames.push('Other');
    }
    let valuesPerDateRange: any = this.getHistoricalValuesPerDateRangeValue();
    let firstDate = this.aggregatedValuesPerDateRange[0].date;
    let firstDateToRender: moment.Moment = this.transactionFilter.startDate == null ?
      this.aggregatedValuesPerDateRange[0].date :
      moment(this.transactionFilter.startDate);
    let lastDateToRender = this.transactionFilter.endDate == null ?
      moment() :
      moment(this.transactionFilter.endDate);

    let currentDate = firstDate;
    let dateRangeFormat = this.getDateRangeFormat();
    let previousSeriesValue: any = {};
    let seriesValuesByName: any = {};
    let lastValueIfNotReset: any = {};
    let allDates: string[] = [];
    let offsetPerGroup = this.getOffsetPerGroup(firstDateToRender, valuesPerDateRange);

    groupNames.forEach((groupName: any) => {
      seriesValuesByName[groupName] = []
      lastValueIfNotReset[groupName] = null;
    });

    // we loop over every date, also outside of the scope of the filter. This is because we need to get the
    // previous values. If we have only a value on the third date index, not on index 0, 1, 2, but we
    // do have historical value on index -1, then we want to know those historical values.
    while (currentDate.isSameOrBefore(lastDateToRender)) {
      let currentRangedString = currentDate.format(dateRangeFormat);
      let withinDateRange = currentDate.isSameOrAfter(firstDateToRender);

      if (withinDateRange) {
        allDates.push(currentDate.format('DD-MM-yyyy'));
      }

      groupNames.forEach(groupName => {
        let groupSeries = seriesValuesByName[groupName];
        let hasNoValueForDateAndGroup = valuesPerDateRange[currentRangedString] == null || valuesPerDateRange[currentRangedString][groupName] == null;
        let value = 0;

        if (hasNoValueForDateAndGroup) {
          let hasNoPreviousValue = previousSeriesValue[groupName] == null;
          value = hasNoPreviousValue ? 0 : previousSeriesValue[groupName];
        } else {
          value = valuesPerDateRange[currentRangedString][groupName].value - this.getOffset(offsetPerGroup, groupName);
        }

        let nonResettedValue = value;

        if (this.transactionFilter.startAtZeroAfterDateAggregation && lastValueIfNotReset[groupName] != null) {
          value -= lastValueIfNotReset[groupName];
        }
        previousSeriesValue[groupName] = nonResettedValue;
        if (withinDateRange) {
          groupSeries.push(value);
        }
        lastValueIfNotReset[groupName] = nonResettedValue;
      });

      let nextDateRangeString = currentRangedString;
      while (nextDateRangeString == currentRangedString) {
        currentDate = currentDate.add(1, 'day');
        nextDateRangeString = currentDate.format(dateRangeFormat);
      }
    }

    let seriesWithGroups: any = this.generateSeriesGroups(seriesValuesByName);
    this.chartOption = this.generateGraphOptions(allDates, seriesWithGroups);
  }

  generateSeriesGroups(seriesValuesByName: any) {
    let colorIndex = 0;
    return Object.keys(seriesValuesByName)
      .map((name: any) => {
        let group = {
          data: seriesValuesByName[name],
          name: name,
          type: 'bar',
          stack: true,
          showSymbol: false,
          itemStyle: {color: Util.getColor(colorIndex)}
        }
        colorIndex++;
        return group;
      })
  }

  generateGraphOptions(allDates: string[], seriesWithGroups: any) {
    return {
      responsive: true,
      maintainAspectRatio: true,
      height: this.height,//'calc(100% - 100px)',
      xAxis: {
        type: 'category',
        data: allDates,
      },
      yAxis: {
        type: 'value',
      },
      series: seriesWithGroups,
      tooltip: this.getTooltip(),
      legend: {
        position: "top",
      },
      dataZoom: [{
        type: 'slider',
      }],
    };
  }

  private getTooltip() {
    return {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter(params: any): any {
        let date = params[0].axisValue;
        let html = '';

        params.forEach((param: any) => {
          if (param.value != 0) {
            html += `${param.marker} ${param.seriesName} <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(param.value)};">${Util.currencyFormat(param.value)}</span><br/>`;
          }
        });

        return `${date}<br/>` + html;
      },
    }
  }

  getHistoricalValuesPerDateRangeValue() {
    let map: any = {};
    this.aggregatedValuesPerDateRange.forEach(value => {
      if (map[value.dateString] == null) {
        map[value.dateString] = {};
      }
      map[value.dateString][value.groupName] = value;
    });
    return map;
  }

  generateOffsets(startAtZeroFromBeginning: boolean): any {
    let offsets: any = {};
    this.aggregatedValuesPerDateRange.forEach(value => {
      if (offsets[value.groupName] == null) {
        offsets[value.groupName] = startAtZeroFromBeginning ? 0 : value.value;
      }
    });
    return offsets;
  }

  getGroupNameOfHistoricalValue(transaction: MoneyTransaction): string {
    if (transaction.groupId != null) {
      return this.groupsById[transaction.groupId].name;
    }
    if (transaction.counterpartAccountName != null) {
      return transaction.counterpartAccountName;
    }
    if (transaction.counterpartAccountNumber != null) {
      return transaction.counterpartAccountNumber;
    }
    return transaction.description;
  }

  getPositiveValue(value: number): number {
    if (value == null || isNaN(value)) {
      return 0;
    }
    return value < 0 ? -value : value;
  }

  getDateRangeFormat() {
    let range = this.transactionFilter.dateRange;
    if (range == TransactionDateRange.WEEK) {
      return 'YYYY-WW';
    } else if (range == TransactionDateRange.MONTH) {
      return 'YYYY-MM';
    } else if (range == TransactionDateRange.YEAR) {
      return 'YYYY';
    }

    return 'YYYY-MM-DD';
  }

  getStartDate() {
    return this.transactionFilter.startDate == null ? null : moment(this.transactionFilter.startDate);
  }

  getEndDate() {
    return this.transactionFilter.endDate == null ? null : moment(this.transactionFilter.endDate);
  }

  getOffsetPerGroup(firstDateToRender: moment.Moment, valuesPerDateRange: any) {
    let offset: any = {};
    let dateString = firstDateToRender.format(this.getDateRangeFormat());
    let dates = Object.keys(valuesPerDateRange);

    for (let date of dates) {
      if (date == dateString) {
        return offset;
      }
      Object.keys(valuesPerDateRange[date]).forEach((groupName: any) => {
        offset[groupName] = valuesPerDateRange[date][groupName].value;
      });
    }
    return offset;
  }

  getOffset(offsetPerGroup: any, groupName: string) {
    if (!this.transactionFilter.startAtZeroFromBeginning) {
      return 0;
    }
    if (offsetPerGroup[groupName] == null) {
      return 0;
    }
    return offsetPerGroup[groupName];
  }
}
