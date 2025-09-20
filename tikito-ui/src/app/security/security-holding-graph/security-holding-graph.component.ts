import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SecurityApi} from "../../api/security-api";
import HistoricalHoldingValue from "../../dto/security/historical-holding-value";
import {Util} from "../../util";
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import * as echarts from "echarts/core";
import {SecurityHoldingGraphDisplayField} from "../../dto/security/security-holding-graph-display-field";
import {SecurityHoldingFilter} from "../../dto/security/security-holding-filter";
import {SecurityUtil} from "../../security-util";
import {TransactionDateRange} from "../../dto/money/money-transactions-filter";
import {AuthService} from "../../service/auth.service";
import moment from "moment";

class Serie {
  dividendValues: number[] = [];
  taxValues: number[] = [];
  adminCostValues: number[] = [];
  worthValues: number[] = [];
  currentHoldingValues: number[] = [];
  priceValues: number[] = [];
  holdingValuePerDate: any = {};
  performanceValues: number[] = [];
  totalCashWithdrawn: number[] = [];
  totalCashInvested: number[] = [];
  holdingValue: HistoricalHoldingValue;
}

@Component({
  selector: 'app-security-holding-graph',
  standalone: true,
  imports: [
    NgxEchartsDirective
  ],
  templateUrl: './security-holding-graph.component.html',
  styleUrl: './security-holding-graph.component.scss',
  providers: [
    provideEchartsCore({echarts}),
  ]
})
export class SecurityHoldingGraphComponent implements OnInit {

  chartOption: any;
  DATE_FORMAT: string = 'DD-MM-yyyy';

  historicalValues: HistoricalHoldingValue[];

  performanceTimes: any = {};
  lastPerformanceName: string;

  @Input()
  startDate: Date | null;

  @Input()
  endDate: Date | null;

  @Input()
  showLegend: boolean;

  @Input()
  showTooltip: boolean;

  @Input()
  height: number;

  @Input()
  securityHoldingFilter: SecurityHoldingFilter;

  @Input()
  onFilterUpdateCallback: EventEmitter<SecurityHoldingFilter>;

  @Output()
  onChartSeriesSelectedCallback: EventEmitter<any> = new EventEmitter();


  initOptions: any = {};
  heightCss: string;
  holdingsPerSecurityId: any = {};
  legendSelected: any;


  constructor(private api: SecurityApi,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.initOptions = this.height == null ? {} : {'height': this.height};
      this.heightCss = this.height == null ? '' : 'height: ' + this.height + 'px;';

      if (this.onFilterUpdateCallback != null) {
        this.onFilterUpdateCallback.subscribe(filter => {
          this.securityHoldingFilter = filter;
          this.resetGraph();
        });
      } else {
        this.resetGraph();
      }
    });
  }

  resetGraph() {
    this.api.getSecurityHoldings().subscribe(holdings => {
      holdings.forEach(holding => this.holdingsPerSecurityId[holding.securityId] = holding);
      this.perf('getHistoricalValues');
      this.api.getHistoricalValues(this.securityHoldingFilter).subscribe(historicalValues => {
        this.historicalValues = historicalValues;
        this.generateGraph();
      });
    });
  }

  getDateRangeFormat() {
    let range = this.securityHoldingFilter.dateRange;
    if (range == TransactionDateRange.WEEK) {
      return 'YYYY-WW';
    } else if (range == TransactionDateRange.MONTH) {
      return 'YYYY-MM';
    } else if (range == TransactionDateRange.YEAR) {
      return 'YYYY';
    }

    return 'DD-MM-YYYY';
  }

  generateHistoricalValuesPerSecurityAndDate() {
    let historicalValuesPerSecurityAndDate: any = {};
    this.historicalValues.forEach(value => {
      if (historicalValuesPerSecurityAndDate[value.securityId] == null) {
        historicalValuesPerSecurityAndDate[value.securityId] = {}
      }
      let formattedDate = moment(value.date).format(this.DATE_FORMAT);

      historicalValuesPerSecurityAndDate[value.securityId][formattedDate] = value;
    });
    return historicalValuesPerSecurityAndDate;
  }

  addToSerie(serie: any, holdingValue: HistoricalHoldingValue, currentDateRangedString: string, startAtZero: boolean, previousHoldingValue: HistoricalHoldingValue) {
    startAtZero = false;
    serie.holdingValuePerDate[currentDateRangedString] = holdingValue;
    serie.dividendValues.push(holdingValue.totalDividend);
    serie.taxValues.push(holdingValue.totalTaxes);
    serie.adminCostValues.push(holdingValue.totalAdministrativeCosts);
    serie.currentHoldingValues.push(holdingValue.price * holdingValue.amount);
    serie.priceValues.push(holdingValue.price);
    serie.worthValues.push(holdingValue.worth);
    serie.totalCashWithdrawn.push(holdingValue.totalCashWithdrawn);
    serie.totalCashInvested.push(holdingValue.totalCashInvested);
    serie.performanceValues.push(
      // startAtZero ?
      //   SecurityUtil.getPerformanceBetween(holdingValue, previousHoldingValue) :
        SecurityUtil.getPerformance(holdingValue));
  }


  startHoldingValueAtZero(holdingValue: HistoricalHoldingValue, previousHoldingValue: HistoricalHoldingValue): HistoricalHoldingValue {
    let newHoldingValue = {...holdingValue};
    newHoldingValue.totalDividend -= previousHoldingValue.totalDividend;
    newHoldingValue.totalTaxes -= previousHoldingValue.totalTaxes;
    newHoldingValue.totalAdministrativeCosts -= previousHoldingValue.totalAdministrativeCosts;
    newHoldingValue.totalCashWithdrawn -= previousHoldingValue.totalCashWithdrawn;
    // newHoldingValue.totalCashInvested -= previousHoldingValue.totalCashInvested;
    return newHoldingValue;
  }

  generateGraph() {
    this.perf('generateHistoricalValuesPerSecurityAndDate');

    if (this.historicalValues.length == 0) {
      this.chartOption = {};
      return;
    }
    let dates: string[] = [];
    let holdingsValuePerDate: any = {};
    let valuesPerSecurityAndDate: any = this.generateHistoricalValuesPerSecurityAndDate();
    let previousValuePerSecurityId: any = {};
    let seriesPerSecurityId: any = {};
    let securityIds = Object.keys(valuesPerSecurityAndDate);

    let firstDateInRange = moment(this.historicalValues[0].date);
    let currentDate = firstDateInRange.subtract(-1, 'day');
    let dateRangeFormat = this.getDateRangeFormat();
    let aggregateDate = this.securityHoldingFilter.dateRange != null;
    let lastDateToRender = moment(this.historicalValues[this.historicalValues.length - 1].date);
    securityIds.forEach(securityId => {
      seriesPerSecurityId[securityId] = new Serie();
      previousValuePerSecurityId[securityId] = new HistoricalHoldingValue();
    });

    this.perf('generateHistoricalValuesPerSecurityAndDate');
    while (currentDate.isSameOrBefore(lastDateToRender)) {
      let currentDateRangedString = currentDate.format(dateRangeFormat);
      let currentDateString = currentDate.format('DD-MM-yyyy');
      let nextDate = currentDate.add(1, 'day');
      let nextDateRangedString = nextDate.format(dateRangeFormat);
      let nextDateAggregation = nextDateRangedString != currentDateRangedString;

      if (aggregateDate && !nextDateAggregation) {
        continue; // skip the rest
      }

      dates.push(currentDateString);

      securityIds.forEach(securityId => {
        let originalHoldingValue = valuesPerSecurityAndDate[securityId][currentDateString];

        if (originalHoldingValue == null) {
          originalHoldingValue = new HistoricalHoldingValue();
          originalHoldingValue.securityId = securityId;
        }

        let holdingValue = {...originalHoldingValue};

        if (aggregateDate && this.securityHoldingFilter.startAtZeroAfterDateAggregation) {
          // holdingValue = this.startHoldingValueAtZero(holdingValue, previousValuePerSecurityId[securityId]);
        }

        this.addToSerie(
          seriesPerSecurityId[securityId],
          holdingValue,
          currentDateRangedString,
          this.securityHoldingFilter.startAtZeroAfterDateAggregation,
          previousValuePerSecurityId[securityId]);

        if (holdingsValuePerDate[currentDateString] == null) {
          holdingsValuePerDate[currentDateString] = {};
        }
        holdingsValuePerDate[currentDateString][holdingValue.securityId] = holdingValue;
        previousValuePerSecurityId[securityId] = originalHoldingValue;
      });


      currentDate = currentDate.add(1, 'day');
    }
    this.perf('generateSeries');
    let series: any = this.generateSeries(seriesPerSecurityId);
    let hiddenSeries: string = Util.getUrlFragment('hiddenSeries') as string;
    let seriesInfo: any = {};
    if (hiddenSeries != null) {
      hiddenSeries.split(',').forEach((series: string) => {
        seriesInfo[series] = false;
      });
    }

    let tooltip = !this.showTooltip ? {} : this.getTooltip(holdingsValuePerDate, this.holdingsPerSecurityId)
// https://echarts.apache.org/examples/en/editor.html?c=multiple-y-axis
    let options: any = {
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: [
        {
          type: 'value',
          position: 'left',
          name: 'money',
          axisLabel: {
            formatter: (value: any) => `${value}`
          }
        },
        {
          type: 'value',
          position: 'right',
          name: 'percentage',
          axisLabel: {
            formatter: (value: any) => `${value}%`
          }
        },
        {
          type: 'value',
          position: 'right',
          name: 'price',
          offset: 50,
          axisLabel: {
            formatter: (value: any) => `${value}`
          }
        }
      ],
      series: series,
      tooltip: tooltip,
      dataZoom: [
        {
          type: this.height < 150 ? 'inside' : 'slider',
        },
      ],
    };

    if (this.showLegend) {
      options['legend'] = {
        position: "top",
        selected: seriesInfo
      }
    }
    this.perf('done');
    this.chartOption = options;
  }

  private generateSeries(seriesPerSecurityId: any) {
    let series: any = [];
    let colorIndex = 0;
    Object.keys(seriesPerSecurityId).forEach((securityId: any) => {
      if (this.showField(SecurityHoldingGraphDisplayField.PRICE)) {
        series.push({
          data: seriesPerSecurityId[securityId].priceValues,
          name: SecurityHoldingGraphDisplayField.PRICE,
          type: 'line',
          yAxisIndex: 2,
          showSymbol: false,
          color: Util.getColor(colorIndex),
          lineStyle: {color: Util.getColor(colorIndex)}
        });
        colorIndex++;
      }

      if (this.showField(SecurityHoldingGraphDisplayField.DIVIDEND)) {
        series.push({
          data: seriesPerSecurityId[securityId].dividendValues,
          name: SecurityHoldingGraphDisplayField.DIVIDEND,
          type: 'line',
          areaStyle: {},
          showSymbol: false,
          color: Util.getColor(colorIndex),
          lineStyle: {color: Util.getColor(colorIndex)}
        });
        colorIndex++;
      }

      if (this.showField(SecurityHoldingGraphDisplayField.HOLDING_VALUE)) {
        series.push({
          data: seriesPerSecurityId[securityId].currentHoldingValues,
          name: SecurityHoldingGraphDisplayField.HOLDING_VALUE,
          areaStyle: {},
          type: 'line',
          showSymbol: false,
          color: Util.getColor(colorIndex),
          lineStyle: {color: Util.getColor(colorIndex)}
        });
        colorIndex++;
      }

      if (this.showField(SecurityHoldingGraphDisplayField.TAX)) {
        series.push({
          data: seriesPerSecurityId[securityId].taxValues,
          name: SecurityHoldingGraphDisplayField.TAX,
          type: 'line',
          areaStyle: {},
          showSymbol: false,
          color: Util.getColor(colorIndex),
          lineStyle: {color: Util.getColor(colorIndex)}
        });
        colorIndex++;
      }

      if (this.showField(SecurityHoldingGraphDisplayField.ADMIN_COST)) {
        series.push({
          data: seriesPerSecurityId[securityId].adminCostValues,
          name: SecurityHoldingGraphDisplayField.ADMIN_COST,
          type: 'line',
          stack: 'y',
          areaStyle: {},
          showSymbol: false,
          color: Util.getColor(colorIndex),
          lineStyle: {color: Util.getColor(colorIndex)}
        });
        colorIndex++;
      }

      if (this.showField(SecurityHoldingGraphDisplayField.PERFORMANCE)) {
        series.push({
          data: seriesPerSecurityId[securityId].performanceValues,
          name: SecurityHoldingGraphDisplayField.PERFORMANCE,
          type: 'line',
          yAxisIndex: 1,
          showSymbol: false,
          color: Util.getColor(colorIndex),
          lineStyle: {color: Util.getColor(colorIndex)}
        });
        colorIndex++;
      }
    });
    return series;
  }

  private showField(field: SecurityHoldingGraphDisplayField): boolean {
    return this.securityHoldingFilter.displayField == null ||
      this.securityHoldingFilter.displayField == field;
  }


  private getTooltip(holdingsValuePerDate: any, holdingsPerSecurityId: any) {
    return {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter(params: any): any {
        let date = moment(params[0].axisValue, 'DD-MM-yyyy').format('DD-MM-yyyy');
        let holdingValue = holdingsValuePerDate[date];

        function getMarker(params: any, field: SecurityHoldingGraphDisplayField) {
          for (let param of params) {
            if (param.seriesName == field) {
              return param.marker;
            }
          }
          return '';
        }

        return '<table>' +
          `<tr><td>${date}</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px;">${holdingsPerSecurityId[holding.securityId].security.name}</span></td>`) + '</tr>' +
          `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.PRICE)} Price * amount</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px;">${Util.currencyFormatWithSymbol(holding.price, holding.currencyId)} * ${holding.amount}</span></td>`) + '</tr>' +
          `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.HOLDING_VALUE)} Current value</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.price * holding.amount)};">${Util.currencyFormatWithSymbol(holding.price * holding.amount, holding.currencyId)}</span></td>`) + '</tr>' +
          // `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.MAX_CASH_INVESTED)} Invested </td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.maxCashInvested)};">${Util.currencyFormatWithSymbol(holding.maxCashInvested, holding.currencyId)}</span></td>`) + '</tr>' +
          `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.ADMIN_COST)} Admin costs</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.totalAdministrativeCosts)};">${Util.currencyFormatWithSymbol(holding.totalAdministrativeCosts, holding.currencyId)}</span></td>`) + '</tr>' +
          `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.TAX)} Taxes</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.totalTaxes)};">${Util.currencyFormatWithSymbol(holding.totalTaxes, holding.currencyId)}</span></td>`) + '</tr>' +
          `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.DIVIDEND)} Dividend</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.totalDividend)};">${Util.currencyFormatWithSymbol(holding.totalDividend, holding.currencyId)}</span></td>`) + '</tr>' +
          // `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.CASH_INVESTED)} Invested </td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.cashInvested)};">${Util.currencyFormatWithSymbol(holding.cashInvested, holding.currencyId)}</span></td>`) + '</tr>' +
          // `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.CASH_ON_HAND)} On hand</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holding.cashOnHand)};">${Util.currencyFormatWithSymbol(holding.cashOnHand, holding.currencyId)}</span></td>`) + '</tr>' +
          `<tr><td>${getMarker(params, SecurityHoldingGraphDisplayField.PERFORMANCE)} Performance</td>` + Object.values(holdingValue).map((holding: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(SecurityUtil.getPerformance(holding))};">${Util.percentageFormat(SecurityUtil.getPerformance(holding))}</span></td>`) + '</tr>' +
          '</table>';
      }
    }
  }

  onLegendSelectedChanged(event: any) {
    this.legendSelected = event.selected;
    this.onChartSeriesSelectedCallback.next(event);
  }

  setSecurityHoldingFilter(securityHoldingFilter: SecurityHoldingFilter): void {
    this.securityHoldingFilter = securityHoldingFilter
  }

  perf(name: string) {
    let now = Date.now();
    this.performanceTimes[name] = now;

    if (this.lastPerformanceName != null) {
      console.log(this.lastPerformanceName + ' took ' + (now - this.performanceTimes[this.lastPerformanceName]));
    }

    this.lastPerformanceName = name;
  }
}
