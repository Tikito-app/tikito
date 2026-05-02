import {Component, EventEmitter, Input, OnInit} from '@angular/core';
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import * as echarts from 'echarts/core';
import {EChartModule} from "../../echart-module";
import {MoneyApi} from "../../api/money-api";
import {Util} from "../../util";
import {MoneyTransactionsFilter, TransactionDateRange} from "../../dto/money/money-transactions-filter";
import moment, {Moment} from "moment";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import MoneyTransaction from "../../dto/money/money-transaction";
import {Observable} from "rxjs";
import {AuthService} from "../../service/auth.service";
import {HistoricalBudgetValue} from "../../dto/money/historical-budget-value";
import {MoneyBudgetTransaction} from "../../dto/money/money-budget-transaction";
import {SecurityApi} from "../../api/security-api";
import {NormalizedMoneyGraphValue} from "../../dto/money/normalized-money-graph-value";
import {MoneyGraphGroupInfo} from "../../dto/money/money-graph-group-info";
import {MoneyGraphGroupKey} from "../../dto/money/money-graph-group-key";
import {MoneyGraphService} from "../../service/money-graph-service";
import {TranslateService} from "../../service/translate.service";


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

  otherGroupName: string;

  moneyTransactions: MoneyTransaction[];
  allTransactions: MoneyBudgetTransaction[];
  historicalBudgetValues: HistoricalBudgetValue[];
  historicalMoneyValuesByCurrencyAndDate: any;
  normalizedValues: NormalizedMoneyGraphValue[];
  aggregatedValuesPerDateRange: NormalizedMoneyGraphValue[];
  groupsByName: any;
  highestValuedGroups: any;
  groupsById: any;
  moneyTransactionGroups: MoneyTransactionGroup[] = [];
  chartOption: any;
  performanceTimes: any = {};
  lastPerformanceName: string;
  accountsOfTransactions: any;
  lastDateRange: TransactionDateRange | null;
  securityPricesByIdAndDate: any = {}
  firstDateOfMoneyHolding: Moment | null;
  moneyGroupsInTransactions: number[] = [];

  constructor(private api: MoneyApi,
              private securityApi: SecurityApi,
              private authService: AuthService,
              private translateService: TranslateService) {
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

  assertHasTransactions(): Observable<void> {
    this.perf('getTransactions');
    // todo, make nicer; find a nice way to reset the transactions when needed
    const accountIdsJson = JSON.stringify(this.transactionFilter.accountIds) + JSON.stringify(this.transactionFilter.currencies) + JSON.stringify(this.transactionFilter.groupIds);
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
      if (!this.transactionFilter.includeBudget) {
        observer.next();
        return;
      }

      this.api.getHistoricalBudgetValues(this.getStartDate(), this.getEndDate() as moment.Moment).subscribe(historicalBudgetValues => {
        this.historicalBudgetValues = historicalBudgetValues
          .filter(value => this.transactionFilter.groupIds == null || this.transactionFilter.groupIds?.length == 0 || this.transactionFilter.groupIds.includes(value.groupId));
        observer.next();
      });
    });
  }

  assertHasMoneyHoldings(): Observable<void> {
    return new Observable(observer => {
      if (!this.transactionFilter.includeMoneyHolding) {
        observer.next();
        return;
      }
      this.api.getHistoricalMoneyValues(this.transactionFilter)
        .subscribe(values => {
          this.historicalMoneyValuesByCurrencyAndDate = {};
          values.forEach(value => {
            if (this.firstDateOfMoneyHolding == null) {
              this.firstDateOfMoneyHolding = moment(value.date);
            }
            if (this.historicalMoneyValuesByCurrencyAndDate[value.currencyId] == null) {
              this.historicalMoneyValuesByCurrencyAndDate[value.currencyId] = {}
            }
            let dateFormatted = moment(value.date).format('DD-MM-yyyy').toString();
            if (this.historicalMoneyValuesByCurrencyAndDate[value.currencyId][dateFormatted] == null) {
              this.historicalMoneyValuesByCurrencyAndDate[value.currencyId][dateFormatted] = value;
            } else {
              this.historicalMoneyValuesByCurrencyAndDate[value.currencyId][dateFormatted].amount += value.amount;
            }
          });
          observer.next();
        })
    });
  }

  resetGraph() {
    this.assertHasTransactions().subscribe(() => {
      this.assertHasBudget().subscribe(() => {
        this.assertHasMoneyHoldings().subscribe(() => {
          this.otherGroupName = this.translateService.translate('money/graph/other-group-name');

          this.perf('save');
          this.generateGroupsByName();
          this.perf('generateGroupsByName');

          this.generateMoneyBudgetTransactions();
          this.perf('generateGroupsByName');

          this.extractGroupsFromTransactions();
          this.perf('extractGroupsFromTransactions');

          MoneyGraphService.generateOtherGroupsByName(this.allTransactions, this.groupsByName, this.transactionFilter);
          this.perf('mapHistoricalMoneyValueToNormalizedMoneyValue');

          this.normalizedValues = MoneyGraphService.mapHistoricalMoneyValueToNormalizedMoneyValue(this.groupsById, this.allTransactions, this.transactionFilter);
          this.perf('calculateNormalizedAggregatedValues');

          MoneyGraphService.calculateNormalizedAggregatedValues(this.allTransactions, this.groupsByName, this.groupsById, this.getStartDate(), this.getEndDate(), this.transactionFilter);
          this.perf('splitGroupsAndMapByName');

          this.highestValuedGroups = MoneyGraphService.splitGroupsAndMapByName(this.groupsByName, this.transactionFilter);
          this.perf('aggregateValues');

          this.aggregatedValuesPerDateRange = MoneyGraphService.aggregateValues(this.normalizedValues, this.highestValuedGroups, this.otherGroupName);
          this.perf('generateGraphOptions');

          this.generateGraph();
          this.perf('end');
        });
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
      this.allTransactions = this.allTransactions.concat(this.historicalBudgetValues.map(budgetValue =>
        MoneyGraphService.mapToMoneyBudgetTransaction(budgetValue, this.groupsById)));
    }
    this.allTransactions.sort((a, b) => moment(a.timestamp).unix() - moment(b.timestamp).unix());
  }

  extractGroupsFromTransactions() {
    this.moneyGroupsInTransactions = [];
    this.allTransactions.forEach(transaction => {
      if (!this.moneyGroupsInTransactions.includes(transaction.groupId)) {
        this.moneyGroupsInTransactions.push(transaction.groupId);
      }
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
      let groupInfo = new MoneyGraphGroupInfo(group.id, group.name, false, false, 0);
      this.groupsByName[groupInfo.key] = groupInfo;
      this.groupsById[group.id] = group;
    });
  }

  getFirstDateOfGraph(): Moment | null {
    if (this.firstDateOfMoneyHolding != null && this.aggregatedValuesPerDateRange[0] != null) {
      return this.firstDateOfMoneyHolding.isBefore(this.aggregatedValuesPerDateRange[0].date) ? this.firstDateOfMoneyHolding : this.aggregatedValuesPerDateRange[0].date;
    } else if (this.aggregatedValuesPerDateRange[0] != null) {
      return this.aggregatedValuesPerDateRange[0].date
    }
    return this.firstDateOfMoneyHolding;
  }

  generateGraph() {
    let groupKeys = Object.keys(this.highestValuedGroups);

    if (this.transactionFilter.showOther) {
      const otherActualKey = new MoneyGraphGroupKey(this.otherGroupName, false, false).toString();
      const otherBudgetKey = new MoneyGraphGroupKey(this.otherGroupName, true, false).toString();

      if (!groupKeys.includes(otherActualKey)) groupKeys.push(otherActualKey);
      if (!groupKeys.includes(otherBudgetKey)) groupKeys.push(otherBudgetKey);
    }

    let valuesPerDateRange: any = this.getAggregatedHistoricalValuesPerDateRangeValue();
    let firstDate = this.getFirstDateOfGraph();

    if (firstDate == null) {
      this.chartOption = {};
      return;
    }

    let firstDateToRender: moment.Moment = this.transactionFilter.startDate == null ?
      firstDate.clone() :
      MoneyGraphService.getDateByDateRange(moment(this.transactionFilter.startDate), this.transactionFilter);
    let lastDateToRender = this.transactionFilter.endDate == null ?
      moment() :
      MoneyGraphService.getDateByDateRange(moment(this.transactionFilter.endDate), this.transactionFilter);

    let currentDate = firstDate.clone();
    let dateRangeFormat = MoneyGraphService.getDateRangeFormat(this.transactionFilter);
    let previousSeriesValue: any = {};
    let previousSeriesCurrency: any = {};
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

      groupKeys
        .filter(key => !MoneyGraphGroupKey.fromString(key).isHolding)
        .forEach(groupKey => {
          let groupSeries = seriesValuesByKey[groupKey];
          let hasNoValueForDateAndGroup = valuesPerDateRange[currentRangedString] == null || valuesPerDateRange[currentRangedString][groupKey] == null;
          let value = 0;
          let currencyId = 0;

          if (hasNoValueForDateAndGroup) {
            let hasNoPreviousValue = previousSeriesValue[groupKey] == null;
            value = hasNoPreviousValue ? 0 : previousSeriesValue[groupKey];
            currencyId = hasNoPreviousValue ? 0 : previousSeriesCurrency[groupKey];
          } else {
            let groupValue = valuesPerDateRange[currentRangedString][groupKey];
            value = groupValue.value - this.getOffset(offsetPerGroup, groupKey);
            currencyId = groupValue.currencyId;
          }

          let nonResettedValue = value;

          if (this.transactionFilter.startAtZeroAfterDateAggregation && lastValueIfNotReset[groupKey] != null) {
            value -= lastValueIfNotReset[groupKey];
          }
          previousSeriesValue[groupKey] = nonResettedValue;
          previousSeriesCurrency[groupKey] = currencyId;
          value = this.applyExchangeRate(currentDate, currencyId, value);
          if (withinDateRange) {
            groupSeries.push(value);
          }
          lastValueIfNotReset[groupKey] = nonResettedValue;
          groupValuePerDate[currentDateString][groupKey] = value;
        });

      MoneyGraphService.generateGroupValuesForMoney(this.historicalMoneyValuesByCurrencyAndDate, groupKeys, seriesValuesByKey, groupValuePerDate, currentDateString);

      currentDate = this.calculateNextCurrentDate(currentDate);
    }

    let seriesWithGroups: any = this.generateSeriesGroups(seriesValuesByKey);
    this.chartOption = this.generateGraphOptions(allDates, seriesWithGroups, groupValuePerDate);
  }

  generateSeriesGroups(seriesValuesByKey: any) {
    let colorIndex = 0;
    const colorMap: { [key: string]: number } = {};
    return Object.keys(seriesValuesByKey)
      .map((key: any) => {
        const groupInfo = this.highestValuedGroups[key] || this.groupsByName[key];
        const groupKeyObject = MoneyGraphGroupKey.fromString(key);
        const name = groupInfo ? groupInfo.name : groupKeyObject.name;
        const isBudget = groupInfo ? groupInfo.isBudget : groupKeyObject.isBudget;
        const isHolding = groupInfo ? groupInfo.isHolding : groupKeyObject.isHolding;

        if (colorMap[name] === undefined) {
          colorMap[name] = colorIndex++;
        }
        const seriesColorIndex = colorMap[name];

        return {
          data: seriesValuesByKey[key],
          name: name,
          type: isHolding ? 'line' : 'bar',
          stack: (isBudget ? 'budget' : (isHolding ? 'money' : 'actual')),
          showSymbol: false,
          itemStyle: this.getItemStyle(seriesColorIndex, isBudget)
        }
      })
  }

  getItemStyle(colorIndex: number, isBudget: boolean) {
    if (isBudget) {
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
          .map(MoneyGraphGroupKey.fromString)
          .filter(key => key.isHolding || allGroups[key.toString()] != 0)
          .map(key => key.name);
        let uniqueGroupNames = [...new Set(groupNames)].sort()

        for (let groupName of uniqueGroupNames) {
          let moneyGroupKey = new MoneyGraphGroupKey(groupName, false, false).toString();
          let budgetGroupKey = new MoneyGraphGroupKey(groupName, true, false).toString();
          let holdingGroupKey = new MoneyGraphGroupKey(groupName, false, true).toString();
          let value = allGroups[holdingGroupKey] != null ? allGroups[holdingGroupKey] : allGroups[moneyGroupKey] != null ? allGroups[moneyGroupKey] : allGroups[budgetGroupKey];

          html += `<tr><td>${getMarker(params, groupName.toString())}</td><td>${Util.maxDisplayString(groupName, 25)}</td><td>`;
          html += `<span style="float: right; margin-left: 20px; color: ${Util.currencyColor(value)};">${Util.currencyFormatWithSymbol(value, 47)}</span></td>`;
          html += (allGroups[budgetGroupKey] != null ? `<span style="float: right; margin-left: 20px; color: ${Util.currencyColor(allGroups[budgetGroupKey])};">${Util.currencyFormatWithSymbol(allGroups[budgetGroupKey], 47)}</span>` : ``) + '</td>';
          html += '</tr>';
        }

        html += '</table>';

        return `${date}<br/>` + html;
      },
    }
  }

  getAggregatedHistoricalValuesPerDateRangeValue() {
    let map: any = {};
    this.aggregatedValuesPerDateRange.forEach(value => {
      if (map[value.dateString] == null) {
        map[value.dateString] = {};
      }
      map[value.dateString][value.groupKey] = value;
    });
    return map;
  }

  calculateNextCurrentDate(currentDate: moment.Moment) {
    switch (this.transactionFilter.dateRange) {
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

  getStartDate(): Moment | null {
    return this.transactionFilter.startDate == null ? null : moment(this.transactionFilter.startDate);
  }

  getEndDate(): Moment | null {
    return this.transactionFilter.endDate == null ? null : moment(this.transactionFilter.endDate);
  }

  getOffsetPerGroup(firstDateToRender: moment.Moment, valuesPerDateRange: any) {
    let offset: any = {};
    let dateRangeFormat = MoneyGraphService.getDateRangeFormat(this.transactionFilter);
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

  applyExchangeRate(date: Moment, currencyId: number, amount: number) {
    if (this.securityPricesByIdAndDate[currencyId] != null) {
      let formattedDate = date.format('yyyy-MM-DD');
      if (this.securityPricesByIdAndDate[currencyId][formattedDate] != null) {
        return this.securityPricesByIdAndDate[currencyId][formattedDate].price * amount;
      }
    }
    return amount;
  }
}
