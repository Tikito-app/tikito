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

  existingMoneyTransactionGroups: any;
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
              private authService: AuthService,
              private translateService: TranslateService) {
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
    const accountIdsJson =
      JSON.stringify(this.transactionFilter.accountIds) +
      JSON.stringify(this.transactionFilter.currencies) +
      this.transactionFilter.includeMoney + 
      JSON.stringify(this.transactionFilter.groupIds);
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
    this.otherGroupName = this.translateService.translate('money/graph/other-group-name');

    this.assertHasTransactions().subscribe(() => {
      this.assertHasBudget().subscribe(() => {
        this.assertHasMoneyHoldings().subscribe(() => {
          this.existingMoneyTransactionGroups = {};
          this.moneyTransactions.forEach(transaction => this.existingMoneyTransactionGroups[transaction.groupId] = true);

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
          console.log(this.normalizedValues)

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
    this.moneyTransactionGroups
      .filter(group => this.existingMoneyTransactionGroups[group.id] != null)
      .forEach(group => {
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
    let seriesValuesByKey: any = {};
    let allDates: string[] = [];
    let cumulativeValuesBeforeFilterStart: any = this.getOffsetPerGroup(firstDateToRender); // Corrected to use the new getOffsetPerGroup
    let groupValuePerDate: any = {}

    let totalCumulativeValueUpToCurrentDate: any = { ...cumulativeValuesBeforeFilterStart }; // Tracks cumulative sum from the very beginning for each group

    groupKeys.forEach((groupKey: any) => {
      seriesValuesByKey[groupKey] = [];
      // Ensure totalCumulativeValueUpToCurrentDate is initialized for all groups
      if (totalCumulativeValueUpToCurrentDate[groupKey] == null) {
        totalCumulativeValueUpToCurrentDate[groupKey] = 0;
      }
    });

    let otherGroupHasValue = false;

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
          let groupValueForCurrentRange = valuesPerDateRange[currentRangedString] ? valuesPerDateRange[currentRangedString][groupKey] : null;

          let valueAddedInCurrentPeriod = 0;
          let currencyId = 0;

          if (groupValueForCurrentRange) {
            // groupValueForCurrentRange.value is the cumulative sum up to this period
            // So, valueAddedInCurrentPeriod is the difference between current cumulative and previous total cumulative
            valueAddedInCurrentPeriod = groupValueForCurrentRange.value - (totalCumulativeValueUpToCurrentDate[groupKey] || 0);
            currencyId = groupValueForCurrentRange.currencyId;
            totalCumulativeValueUpToCurrentDate[groupKey] = groupValueForCurrentRange.value; // Update total cumulative
          } else {
            // No transactions in this period, cumulative value remains the same
            valueAddedInCurrentPeriod = 0;
            // currencyId remains the same as previous, or 0 if no previous
            // totalCumulativeValueUpToCurrentDate[groupKey] remains unchanged
          }

          let valueToPushToSeries = 0;
          if (this.transactionFilter.startAtZeroFromBeginning) {
            // If starting at zero, the value is the current total cumulative minus the offset
            valueToPushToSeries = (totalCumulativeValueUpToCurrentDate[groupKey] || 0) - (cumulativeValuesBeforeFilterStart[groupKey] || 0);
          } else {
            // Otherwise, just push the value added in this specific period
            valueToPushToSeries = valueAddedInCurrentPeriod;
          }

          valueToPushToSeries = this.applyExchangeRate(currentDate, currencyId, valueToPushToSeries);

          if (withinDateRange) {
            groupSeries.push(valueToPushToSeries);
          }
          groupValuePerDate[currentDateString][groupKey] = valueToPushToSeries;

          if(MoneyGraphGroupKey.fromString(groupKey).name == this.otherGroupName && valueToPushToSeries != 0) {
            otherGroupHasValue = true;
          }
        });

      MoneyGraphService.generateGroupValuesForMoney(this.historicalMoneyValuesByCurrencyAndDate, groupKeys, seriesValuesByKey, groupValuePerDate, currentDateString);

      currentDate = this.calculateNextCurrentDate(currentDate);
    }

    let seriesWithGroups: any = this.generateSeriesGroups(seriesValuesByKey, otherGroupHasValue);
    this.chartOption = this.generateGraphOptions(allDates, seriesWithGroups, groupValuePerDate);
  }

  generateSeriesGroups(seriesValuesByKey: any, otherGroupHasValue: boolean) {
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
          itemStyle: this.getItemStyle(seriesColorIndex, isBudget),
          lineStyle: isHolding ? {
            normal: {
                width: 0.3,
              }
            } : null,
          areaStyle: isHolding ? {
            opacity: 0.3
          } : null,
        }
      })
      .filter(serie => otherGroupHasValue || serie.name != this.otherGroupName)
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

  getOffsetPerGroup(firstDateToRender: moment.Moment) {
    let offset: any = {};
    // Iterate through the already aggregated values to find the cumulative sum just before firstDateToRender
    this.aggregatedValuesPerDateRange.forEach(value => {
      if (value.date.isBefore(firstDateToRender)) {
        offset[value.groupKey] = value.value; // Store the cumulative value for this group
      }
    });
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
