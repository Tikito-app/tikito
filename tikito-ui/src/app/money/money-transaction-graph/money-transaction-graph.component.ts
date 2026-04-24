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
import {HistoricalBudgetValue} from "../../dto/money/historical-budget-value";
import {MoneyBudgetTransaction} from "../../dto/money/money-budget-transaction";

class GroupKey {
  name: string;
  isBudget: boolean;

  constructor(name: string, isBudget: boolean) {
    this.name = name;
    this.isBudget = isBudget;
  }

  toString(): string {
    return this.name + '||' + this.isBudget;
  }

  static fromString(key: string): GroupKey {
    let parts = key.split('||');
    return new GroupKey(parts[0], parts[1] == 'true');
  }
}

class GroupInfo {
  id: number
  name: string;
  isBudget: boolean;
  key: string;
  normalizedAggregatedValue: number = 0;

  constructor(id: number, name: string, isBudget: boolean) {
    this.id = id;
    this.name = name;
    this.isBudget = isBudget;
    this.key = new GroupKey(name, isBudget).toString();
  }
}

export class NormalizedMoneyValue {
  dateString: string;
  date: moment.Moment;
  value: number;
  groupKey: string;
  previous: NormalizedMoneyValue | null;

  constructor(dateString: string, date: moment.Moment, value: number, groupKey: string, previous: NormalizedMoneyValue | null) {
    this.dateString = dateString;
    this.date = date;
    this.value = value;
    this.groupKey = groupKey;
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

  @Input()
  transactionFilter: MoneyTransactionsFilter;

  @Input()
  onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter>;

  moneyTransactions: MoneyTransaction[];
  allTransactions: MoneyBudgetTransaction[];
  historicalBudgetValues: HistoricalBudgetValue[];
  normalizedValues: NormalizedMoneyValue[];
  aggregatedValuesPerDateRange: NormalizedMoneyValue[];
  groupsByName: any;
  highestValuedGroups: any;
  groupsById: any;
  moneyTransactionGroups: MoneyTransactionGroup[] = [];
  chartOption: any;
  performanceTimes: any = {};
  lastPerformanceName: string;
  accountsOfTransactions: any;
  lastDateRange: TransactionDateRange | null;

  constructor(private api: MoneyApi,
              private authService: AuthService) {
  }

  perf(name: string) {
    let now = Date.now();
    this.performanceTimes[name] = now;

    if (this.lastPerformanceName != null) {
      // console.log(this.lastPerformanceName + ' took ' + (now - this.performanceTimes[this.lastPerformanceName]));
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

  assertHasTransactions(): Observable<void> {
    this.perf('getTransactions');
    // todo, make nicer; find a nice way to reset the transactions when needed
    const accountIdsJson = JSON.stringify(this.transactionFilter.accountIds) + JSON.stringify(this.transactionFilter.groupIds);
    if (this.accountsOfTransactions !== accountIdsJson || this.lastDateRange !== this.transactionFilter.dateRange) {
      this.moneyTransactions = [];
      this.allTransactions = [];
    }
    this.accountsOfTransactions = accountIdsJson;
    this.lastDateRange = this.transactionFilter.dateRange;

    return new Observable(observer => {
      if ((this.moneyTransactions != null && this.moneyTransactions.length > 0) || !this.transactionFilter.includeMoney) {
        observer.next();
      } else {
        this.api.getTransactions(this.transactionFilter.withoutStartAndEndDate()).subscribe(transactions => {
          this.moneyTransactions = transactions || [];
          observer.next();
        });
      }
    });
  }

  assertHasBudget(): Observable<void> {
    this.historicalBudgetValues = [];
    return new Observable(observer => {
      if(!this.transactionFilter.includeBudget) {
        observer.next();
        return;
      }

      let startDate = this.getStartDate();
      if (startDate == null) {
        startDate = moment().subtract(1, 'year');
      }


      this.api.getHistoricalBudgetValues(this.getDateByDateRange(startDate), this.getEndDate() as moment.Moment).subscribe(historicalBudgetValues => {
        this.historicalBudgetValues = historicalBudgetValues
          .filter(value => this.transactionFilter.groupIds == null || this.transactionFilter.groupIds?.length == 0 || this.transactionFilter.groupIds.includes(value.groupId));
        observer.next();
      });
    });
  }

  resetGraph() {
    this.assertHasTransactions().subscribe(() => {
      this.assertHasBudget().subscribe(() => {
        this.perf('save');
        this.generateGroupsByName()

        this.generateMoneyBudgetTransactions();

        this.perf('generateGroupsByName');
        this.generateOtherGroupsByName();
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
    });
  }

  generateMoneyBudgetTransactions() {
    this.allTransactions = [];
    if (this.transactionFilter.includeMoney && this.moneyTransactions) {
      this.allTransactions = this.allTransactions.concat(this.moneyTransactions.map(transaction => {
        const t = {...transaction} as MoneyBudgetTransaction;
        t.budgeted = undefined as any;
        return t;
      }));
    }
    if (this.transactionFilter.includeBudget && this.historicalBudgetValues) {
      this.allTransactions = this.allTransactions.concat(this.historicalBudgetValues.map(budgetValue => this.mapToMoneyBudgetTransaction(budgetValue)));
    }
    this.allTransactions.sort((a, b) => moment(a.timestamp).unix() - moment(b.timestamp).unix());
  }

  mapToMoneyBudgetTransaction(budgetValue: HistoricalBudgetValue): MoneyBudgetTransaction {
    let transaction = {...budgetValue} as unknown as MoneyBudgetTransaction;
    transaction.amount = budgetValue.budgeted;
    transaction.timestamp = budgetValue.date;
    const group = this.groupsById[budgetValue.groupId];
    transaction.counterpartAccountName = group ? group.name : ('Group ' + budgetValue.groupId);
    transaction.budgeted = budgetValue.budgeted;
    transaction.groupId = budgetValue.groupId;
    return transaction;
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
      let groupInfo = new GroupInfo(group.id, group.name, false);
      this.groupsByName[groupInfo.key] = groupInfo;
      this.groupsById[group.id] = group;
    });
  }

  generateOtherGroupsByName() {
    // now put all the non-grouped values in it by the counterparty name
    this
      .allTransactions
      .filter(value => value.groupId == null || value.budgeted != null)
      .forEach(value => {
        const groupName = this.getGroupNameOfHistoricalValue(value);
        const isBudget = value.budgeted != null;
        const key = new GroupKey(groupName, isBudget).toString();
        if (!this.groupsByName[key]) {
          const id = value.groupId != null ? value.groupId : -1;
          this.groupsByName[key] = new GroupInfo(id, groupName, isBudget);
        }
      });
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
        let dateRange = this.getDateByDateRange(date);
        const isBudget = value.budgeted != null;

        return new NormalizedMoneyValue(
          dateRangeString,
          dateRange,
          value.amount,
          new GroupKey(this.getGroupNameOfHistoricalValue(value), isBudget).toString(),
          null);
      });
  }

  /**
   * Sums up the positive value for each grouped and non-grouped value in the groupsByName.
   */
  calculateNormalizedAggregatedValues() {
    let startDate = this.getStartDate();
    let endDate = this.getEndDate();
    
    // Align filter dates to period boundaries for consistent weight calculation
    let alignedStartDate = startDate ? this.getDateByDateRange(startDate) : null;
    let alignedEndDate = endDate ? this.getDateByDateRange(endDate).endOf(this.getPeriodUnit()) : null;

    this.allTransactions
      .filter(value => alignedStartDate == null || moment(value.timestamp).isSameOrAfter(alignedStartDate))
      .filter(value => alignedEndDate == null || moment(value.timestamp).isSameOrBefore(alignedEndDate))
      .forEach(value => {
        const isBudget = value.budgeted != null;
        const key = new GroupKey(this.getGroupNameOfHistoricalValue(value), isBudget).toString();
        if (this.groupsByName[key]) {
          this.groupsByName[key].normalizedAggregatedValue += this.getPositiveValue(value.amount);
        }
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
      .forEach((group: any) => this.highestValuedGroups[group.key] = group);

    // now add the user defined groups
    Object.values(this.groupsByName)
      .filter((group: any) => group.id != -1)
      .forEach((group: any) => {
        this.highestValuedGroups[group.key] = group;
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
      .forEach(value => {
        let isLowestGroupValue = this.highestValuedGroups[value.groupKey] == null;
        if (isLowestGroupValue) {
          const groupKeyObject = GroupKey.fromString(value.groupKey);
          value.groupKey = new GroupKey('Other', groupKeyObject.isBudget).toString();
        }

        let dateString = value.dateString;
        if (valuesPerGroupAndDateRange[value.groupKey] == null) {
          valuesPerGroupAndDateRange[value.groupKey] = {};
        }
        if (valuesPerGroupAndDateRange[value.groupKey][dateString] == null) {
          let startValue = previousValuesPerGroup[value.groupKey] == null ? 0 : previousValuesPerGroup[value.groupKey].value;
          valuesPerGroupAndDateRange[value.groupKey][dateString] = new NormalizedMoneyValue(
            dateString,
            value.date,
            startValue,
            value.groupKey,
            previousValuesPerGroup[value.groupKey]);
          previousValuesPerGroup[value.groupKey] = valuesPerGroupAndDateRange[value.groupKey][dateString];
        }
        valuesPerGroupAndDateRange[value.groupKey][dateString].value += value.value;
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
    let groupKeys = Object.keys(this.highestValuedGroups);
    if (this.transactionFilter.showOther) {
      const otherActualKey = new GroupKey('Other', false).toString();
      const otherBudgetKey = new GroupKey('Other', true).toString();
      if (!groupKeys.includes(otherActualKey)) groupKeys.push(otherActualKey);
      if (!groupKeys.includes(otherBudgetKey)) groupKeys.push(otherBudgetKey);
    }
    let valuesPerDateRange: any = this.getHistoricalValuesPerDateRangeValue();
    if (this.aggregatedValuesPerDateRange.length == 0) {
      this.chartOption = {};
      return;
    }

    let firstDate = this.aggregatedValuesPerDateRange[0].date;
    let firstDateToRender: moment.Moment = this.transactionFilter.startDate == null ?
      this.aggregatedValuesPerDateRange[0].date.clone() :
      this.getDateByDateRange(moment(this.transactionFilter.startDate));
    let lastDateToRender = this.transactionFilter.endDate == null ?
      moment() :
      this.getDateByDateRange(moment(this.transactionFilter.endDate));

    let currentDate = firstDate.clone();
    let dateRangeFormat = this.getDateRangeFormat();
    let previousSeriesValue: any = {};
    let seriesValuesByKey: any = {};
    let lastValueIfNotReset: any = {};
    let allDates: string[] = [];
    let offsetPerGroup = this.getOffsetPerGroup(firstDateToRender, valuesPerDateRange);
    let groupValuePerDate: any = {}

    groupKeys.forEach((groupKey: any) => {
      seriesValuesByKey[groupKey] = []
      lastValueIfNotReset[groupKey] = null;
    });

    while (currentDate.isSameOrBefore(lastDateToRender)) {
      let currentRangedString = currentDate.format(dateRangeFormat);
      let currentDateString = currentDate.format('DD-MM-yyyy');
      let withinDateRange = currentDate.isSameOrAfter(firstDateToRender);
      groupValuePerDate[currentDateString] = {};

      if (withinDateRange) {
        allDates.push(currentDate.format('DD-MM-yyyy'));
      }

      groupKeys.forEach(groupKey => {
        let groupSeries = seriesValuesByKey[groupKey];
        let hasNoValueForDateAndGroup = valuesPerDateRange[currentRangedString] == null || valuesPerDateRange[currentRangedString][groupKey] == null;
        let value = 0;

        if (hasNoValueForDateAndGroup) {
          let hasNoPreviousValue = previousSeriesValue[groupKey] == null;
          value = hasNoPreviousValue ? 0 : previousSeriesValue[groupKey];
        } else {
          value = valuesPerDateRange[currentRangedString][groupKey].value - this.getOffset(offsetPerGroup, groupKey);
        }

        let nonResettedValue = value;

        if (this.transactionFilter.startAtZeroAfterDateAggregation && lastValueIfNotReset[groupKey] != null) {
          value -= lastValueIfNotReset[groupKey];
        }
        previousSeriesValue[groupKey] = nonResettedValue;
        if (withinDateRange) {
          groupSeries.push(value);
        }
        lastValueIfNotReset[groupKey] = nonResettedValue;
        groupValuePerDate[currentDateString][groupKey] = value;
      });

      currentDate = this.calculateNextCurrentDate(currentDate);
    }

    let seriesWithGroups: any = this.generateSeriesGroups(seriesValuesByKey);
    this.chartOption = this.generateGraphOptions(allDates, seriesWithGroups, groupValuePerDate);
  }

  generateSeriesGroups(seriesValuesByKey: any) {
    let colorIndex = 0;
    const colorMap: {[key: string]: number} = {};
    return Object.keys(seriesValuesByKey)
      .map((key: any) => {
        const groupInfo = this.highestValuedGroups[key] || this.groupsByName[key];
        const groupKeyObject = GroupKey.fromString(key);
        const name = groupInfo ? groupInfo.name : groupKeyObject.name;
        const isBudget = groupInfo ? groupInfo.isBudget : groupKeyObject.isBudget;

        if (colorMap[name] === undefined) {
          colorMap[name] = colorIndex++;
        }
        const seriesColorIndex = colorMap[name];

        return {
          data: seriesValuesByKey[key],
          name: name,
          type: 'bar',
          stack: isBudget ? 'budget' : 'actual',
          showSymbol: false,
          itemStyle: this.getItemStyle(seriesColorIndex, isBudget)
        }
      })
  }

  getItemStyle(colorIndex: number, isBudget: boolean) {
    if(isBudget) {
      return {
        color: {
          image: this.createStripePattern(Util.getColor(colorIndex)),
          repeat: 'repeat'
        },
        opacity: 0.3
      }
    }

    return {
      color: Util.getColor(colorIndex),
    }
  }

  generateGraphOptions(allDates: string[], seriesWithGroups: any, groupValuePerDate: any) {
    return {
      responsive: true,
      maintainAspectRatio: true,
      height: this.height,
      xAxis: {
        type: 'category',
        data: allDates,
      },
      yAxis: {
        type: 'value',
      },
      series: seriesWithGroups,
      tooltip: this.getTooltip(groupValuePerDate),
      legend: {
        position: "top",
      },
      dataZoom: [{
        type: 'slider',
      }],
    };
  }

  private getTooltip(groupValuePerDate: any) {
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
        let allGroups = groupValuePerDate[date];

        function getMarker(params: any, field: string) {
          for (let param of params) {
            if (param.seriesName == field) {
              return param.marker;
            }
          }
          return '';
        }
        let html = '<table><tr><td></td><td></td><td><span style="float: right; margin-left: 20px;">Spent</span></td><td><span style="float: right; margin-left: 20px;">Budgeted</span></td></tr>';

        let groupNames = Object.keys(allGroups)
          .filter(key => allGroups[key] != 0)
          .map(GroupKey.fromString)
          .map(key => key.name);
        let uniqueGroupNames = [...new Set(groupNames)].sort()


        for(let groupName of uniqueGroupNames) {
          let moneyGroupKey = new GroupKey(groupName, false).toString();
          let budgetGroupKey = new GroupKey(groupName, true).toString();
          html += `<tr><td>${getMarker(params, groupName.toString())}</td><td>${Util.maxDisplayString(groupName, 25)}</td><td>`;
          html += (allGroups[moneyGroupKey] != null ? `<span style="float: right; margin-left: 20px; color: ${Util.currencyColor(allGroups[moneyGroupKey])};">${Util.currencyFormatWithSymbol(allGroups[moneyGroupKey], 47)}</span>` : ``) + '</td><td>';
          html += (allGroups[budgetGroupKey] != null ? `<span style="float: right; margin-left: 20px; color: ${Util.currencyColor(allGroups[budgetGroupKey])};">${Util.currencyFormatWithSymbol(allGroups[budgetGroupKey], 47)}</span>` : ``) + '</td>';
          html += '</tr>';
        }
        html += '</table>';

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
      map[value.dateString][value.groupKey] = value;
    });
    return map;
  }

  generateOffsets(startAtZeroFromBeginning: boolean): any {
    let offsets: any = {};
    this.aggregatedValuesPerDateRange.forEach(value => {
      if (offsets[value.groupKey] == null) {
        offsets[value.groupKey] = startAtZeroFromBeginning ? 0 : value.value;
      }
    });
    return offsets;
  }

  getGroupNameOfHistoricalValue(transaction: MoneyTransaction): string {
    if (transaction.groupId != null && this.groupsById[transaction.groupId]) {
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

  getPeriodUnit() {
    switch (this.transactionFilter.dateRange) {
      case TransactionDateRange.YEAR: return 'year';
      case TransactionDateRange.MONTH: return 'month';
      case TransactionDateRange.WEEK: return 'isoWeek';
      default: return 'day';
    }
  }

  calculateNextCurrentDate(currentDate: moment.Moment) {
    switch(this.transactionFilter.dateRange) {
      case TransactionDateRange.YEAR:
        return currentDate.add(1, 'year').startOf('year');
      case TransactionDateRange.MONTH:
        return currentDate.add(1, 'month').startOf('month');
      case TransactionDateRange.WEEK:
        return currentDate.add(1, 'week').startOf('isoWeek');
      default:
        return currentDate.add(1, 'day');
    }
  }

  getDateByDateRange(date: moment.Moment) {
    return date.clone().startOf(this.getPeriodUnit() as any);
  }

  getStartDate() {
    return this.transactionFilter.startDate == null ? null : moment(this.transactionFilter.startDate);
  }

  getEndDate() {
    return this.transactionFilter.endDate == null ? null : moment(this.transactionFilter.endDate);
  }

  getOffsetPerGroup(firstDateToRender: moment.Moment, valuesPerDateRange: any) {
    let offset: any = {};
    let dateRangeFormat = this.getDateRangeFormat();
    let dateString = firstDateToRender.format(dateRangeFormat);
    let dates = Object.keys(valuesPerDateRange).sort();

    for (let date of dates) {
      if (date == dateString) {
        return offset;
      }
      Object.keys(valuesPerDateRange[date]).forEach((groupKey: any) => {
        offset[groupKey] = valuesPerDateRange[date][groupKey].value;
      });
    }
    return offset;
  }

  getOffset(offsetPerGroup: any, groupKey: string) {
    if (!this.transactionFilter.startAtZeroFromBeginning) {
      return 0;
    }
    if (offsetPerGroup[groupKey] == null) {
      return 0;
    }
    return offsetPerGroup[groupKey];
  }

  createStripePattern(color: string) {
    const canvas = document.createElement('canvas');
    let size = 5;
    canvas.width = size;
    canvas.height = size;
    const ctx: any = canvas.getContext('2d');

    // background transparent
    ctx.strokeStyle = color; // line color
    ctx.lineWidth = 2;

    // diagonal line
    ctx.beginPath();
    ctx.moveTo(0, size);
    ctx.lineTo(size, 0);
    ctx.stroke();

    return canvas;
  }
}
