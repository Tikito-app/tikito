import {Component, EventEmitter, Input, OnInit} from '@angular/core';
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import {MoneyTransactionsFilter, TransactionDateRange} from "../../dto/money/money-transactions-filter";
import {MoneyApi} from "../../api/money-api";
import {AuthService} from "../../service/auth.service";
import {TranslateService} from "../../service/translate.service";
import {MoneyGraphProcessor} from "./money-graph-processor";
import {MoneyGraphDataFetcher} from "./money-graph-data-fetcher";
import {MoneyGraphDto} from "./money-graph-dto";
import {Moment} from "moment/moment";
import moment from "moment";
import {MoneyGraphService} from "../../service/money-graph-service";
import {EChartModule} from "../../echart-module";
import * as echarts from "echarts/core";
import {MoneyGraphGroupKey} from "../../dto/money/money-graph-group-key";
import {Util} from "../../util";

@Component({
  selector: 'app-money-graph',
  standalone: true,
  imports: [
    NgxEchartsDirective,
    EChartModule
  ],
  templateUrl: './money-graph.component.html',
  styleUrl: './money-graph.component.scss',
  providers: [
    provideEchartsCore({echarts}),
  ]
})
export class MoneyGraphComponent implements OnInit {
  @Input()
  accountId: number;

  @Input()
  height: number;

  @Input()
  transactionFilter: MoneyTransactionsFilter;

  @Input()
  onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter>;

  shouldReloadTransactions: boolean = true;
  chartOption: any;
  lastDateRange: TransactionDateRange | null;
  otherGroupName: string;

  dataDto = new MoneyGraphDto();


  constructor(private authService: AuthService,
              private translateService: TranslateService,
              private dataFetcher: MoneyGraphDataFetcher) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.onFilterUpdateCallback.subscribe(filter => {
        this.transactionFilter = filter;
        this.otherGroupName = this.translateService.translate('money/graph/other-group-name');
        this.resetGraph();
      });
    });
  }

  resetGraph(): void {
    if(this.shouldReloadTransactions || this.lastDateRange !== this.transactionFilter.dateRange) {
      this.dataDto = new MoneyGraphDto();
    }

    this.dataFetcher.assertHasData(this.dataDto, this.transactionFilter).subscribe(() => {
      this.processData();
    });
  }

  processData(): void {
    this.lastDateRange = this.transactionFilter.dateRange;

    // 1. Convert cash to historicalCashValuesByCurrencyAndDate[currencyId][date] = amount
    MoneyGraphProcessor.generateHistoricalCashValuesByCurrencyAndDate(this.dataDto);

    // 2.a Concat money transactions and historical budget values into moneyTransactionsWithBudget
    // 2.b Map to MoneyBudgetTransaction
    MoneyGraphProcessor.generateMoneyBudgetTransactions(this.dataDto, this.transactionFilter);

    // 3. map money groups to MoneyGraphGroupInfo and fill moneyGroupsByName and groupNameById
    MoneyGraphProcessor.processMoneyGroups(this.dataDto);

    // 4. Map other transactions to MoneyGraphGroupInfo
    MoneyGraphProcessor.generateOtherGroupsByName(this.dataDto, this.transactionFilter);

    // 5. Calculate the normalizedMutatedAmount for each group
    MoneyGraphProcessor.calculateNormalizedAggregatedGroupValues(this.dataDto, this.transactionFilter);

    // 6. Extract the groups with the highest mutation
    MoneyGraphProcessor.extractHighestGroupsByKey(this.dataDto, this.transactionFilter);

    // 7. Map transactions to MoneyGraphValue
    MoneyGraphProcessor.mapTransactionsToGraphValues(this.dataDto, this.transactionFilter);

    // 8. Sum up the MoneyGraphValues per date range for both the highest groups and the other groups
    MoneyGraphProcessor.sumValuesPerGroupAndDateRange(this.dataDto, this.otherGroupName);

    // 9. Fill in gaps, e.g. if we should not start from 0 after the date range, or if we have an offset, this will be set in moneyValuesPerGroupAndDateRange
    MoneyGraphProcessor.fillInGapsForGroups(this.dataDto, this.transactionFilter, this.getFirstDateToRender(), this.getEndDateToRender());

    // 10. generate the graph
    // todo: make static
    this.processDates();

    let series = this.generateSeriesJson();

    this.chartOption = this.generateGraphOptions(series);
    console.log(this.dataDto);
  }

  processDates() {
    this.dataDto.seriesPerGroupKey = {};

    let firstDateToRender = this.getFirstDateToRender();
    let lastDateToRender = this.getEndDateToRender();
    let currentDate = firstDateToRender.clone();
    let dateRangeFormat = MoneyGraphService.getDateRangeFormat(this.transactionFilter);

    Object.keys(this.dataDto.moneyValuesPerGroupAndDateRange).forEach(key => {
      this.dataDto.seriesPerGroupKey[key] = [];
    })


    while (currentDate.isSameOrBefore(lastDateToRender)) {
      let currentRangedString = currentDate.format(dateRangeFormat);
      let currentDateString = currentDate.format('DD-MM-yyyy');

      this.dataDto.allDates.push(currentDateString);

      this.generateSeriesForDate(currentRangedString);

      currentDate = MoneyGraphProcessor.calculateNextCurrentDate(currentDate, this.transactionFilter);
    }
  }

  generateSeriesForDate(currentRangedString: string) {
    Object.keys(this.dataDto.moneyValuesPerGroupAndDateRange).forEach((key: any) => {
      let serie = this.dataDto.seriesPerGroupKey[key];
      let value = 0;
      if(this.dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString] != null) {
        value = this.dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString].value;
      }

      serie.push(value);
    });
  }

  generateSeriesJson() {
    let colorIndex = 0;
    const colorMap: { [key: string]: number } = {};
    return Object.keys(this.dataDto.seriesPerGroupKey)
      .map((key: any) => {
        let groupKey = MoneyGraphGroupKey.fromString(key);
        const groupInfo = this.dataDto.moneyGroupsByKey[key];
        const name = groupInfo ? groupInfo.name : groupKey.name;
        const isBudget = groupInfo ? groupInfo.isBudget : groupKey.isBudget;
        const isHolding = groupInfo ? groupInfo.isHolding : groupKey.isHolding;

        if (colorMap[name] === undefined) {
          colorMap[name] = colorIndex++;
        }
        const seriesColorIndex = colorMap[name];

        return {
          data: this.dataDto.seriesPerGroupKey[key],
          name: name,
          type: isHolding ? 'line' : 'bar',
          stack: (isBudget ? 'budget' : (isHolding ? 'money' : 'actual')),
          showSymbol: false,
          itemStyle: MoneyGraphComponent.getItemStyle(seriesColorIndex, isBudget),
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
  }

  generateGraphOptions(series: any) {
    return {
      responsive: true,
      maintainAspectRatio: true,
      height: this.height,
      xAxis: {
        type: 'category',
        data: this.dataDto.allDates,
      },
      yAxis: {
        type: 'value',
      },
      series: series,
      tooltip: this.getTooltip(this.dataDto, MoneyGraphService.getDateRangeFormat(this.transactionFilter)),
      legend: {
        position: "top",
      },
      dataZoom: [{
        type: 'slider',
      }],
    };
  }


  private getTooltip(dataDto: MoneyGraphDto, dateRangeFormat: string) {
    return {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter(params: any): any {
        let date = moment(params[0].axisValue, 'DD-MM-yyyy').format(dateRangeFormat);
        function getMarker(params: any, field: string) {
          for (let param of params) {
            if (param.seriesName == field) {
              return param.marker;
            }
          }
          return '';
        }

        let html = '<table><tr><td></td><td></td><td><span style="float: right; margin-left: 20px;">Spent</span></td><td><span style="float: right; margin-left: 20px;">Budgeted</span></td></tr>';

        let groupNames = Object.keys(dataDto.moneyValuesPerGroupAndDateRange)
          .map(MoneyGraphGroupKey.fromString)
          .filter(key => key.isHolding || dataDto.moneyValuesPerGroupAndDateRange[key.toString()] != 0)
          .map(key => key.name);
        let uniqueGroupNames = [...new Set(groupNames)].sort()

        for (let groupName of uniqueGroupNames) {
          let moneyGroupKey = new MoneyGraphGroupKey(groupName, false, false).toString();
          let budgetGroupKey = new MoneyGraphGroupKey(groupName, true, false).toString();
          let holdingGroupKey = new MoneyGraphGroupKey(groupName, false, true).toString();

          let moneyValue = MoneyGraphComponent.getGraphValue(dataDto, moneyGroupKey, date);
          let cashHoldingValue = MoneyGraphComponent.getGraphValue(dataDto, holdingGroupKey, date);
          let budgetValue = MoneyGraphComponent.getGraphValue(dataDto, budgetGroupKey, date);

          let firstValue = moneyValue != null ? moneyValue : cashHoldingValue;

          if(firstValue != null || budgetValue != null) {
            html += `<tr><td>${getMarker(params, groupName.toString())}</td><td>${Util.maxDisplayString(groupName, 25)}</td><td>`;
            html += (firstValue != null ? `<span style="float: right; margin-left: 20px; color: ${Util.currencyColor(firstValue.value)};">${Util.currencyFormatWithSymbol(firstValue.value, firstValue.currencyId)}</span>` : ``) + '</td><td>';
            html += (budgetValue != null ? `<span style="float: right; margin-left: 20px; color: ${Util.currencyColor(budgetValue.value)};">${Util.currencyFormatWithSymbol(budgetValue.value, budgetValue.currencyId)}</span>` : ``) + '</td>';
            html += '</tr>';
          }
        }

        html += '</table>';

        return `${date}<br/>` + html;
      },
    }
  }

  static getGraphValue(dataDto: MoneyGraphDto, key: string, date: string) {
    if(dataDto.moneyValuesPerGroupAndDateRange[key] != null && dataDto.moneyValuesPerGroupAndDateRange[key][date] != null) {
      return dataDto.moneyValuesPerGroupAndDateRange[key][date];
    }
    return null;
  }

  getFirstDateToRender() {
    let firstDateOfGraph = this.transactionFilter.startDate == null ? this.getFirstDateOfData() : this.transactionFilter.getStartDate();
    return MoneyGraphService.getDateByDateRange(firstDateOfGraph as Moment, this.transactionFilter);
  }

  getEndDateToRender() {
    let endDateOfGraph = this.transactionFilter.endDate == null ? moment() : this.transactionFilter.getEndDate();
    return MoneyGraphService.getDateByDateRange(endDateOfGraph as Moment, this.transactionFilter);
  }

  getFirstDateOfData(): Moment | null {
    let firstMoneyTransactionDate = this.dataDto.moneyTransactionsInRange.length > 0 ? moment(this.dataDto.moneyTransactionsInRange[0].timestamp) : null;
    let firstHistoricalCashDate = this.dataDto.historicalCashValues.length > 0 ? moment(this.dataDto.historicalCashValues[0].date) : null;

    if (firstHistoricalCashDate != null && firstMoneyTransactionDate != null) {
      return firstHistoricalCashDate.isBefore(firstMoneyTransactionDate) ? firstHistoricalCashDate : firstMoneyTransactionDate;
    } else if (firstMoneyTransactionDate != null) {
      return firstMoneyTransactionDate
    }
    return firstHistoricalCashDate;
  }

  static getItemStyle(colorIndex: number, isBudget: boolean) {
    if (isBudget) {
      return {
        color: {
          image: MoneyGraphComponent.createStripePattern(Util.getColor(colorIndex)),
          repeat: 'repeat'
        },
        opacity: 0.3
      }
    }

    return {
      color: Util.getColor(colorIndex),
    }
  }


  static createStripePattern(color: string) {
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
