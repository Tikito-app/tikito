import {Component, EventEmitter, Input, OnInit} from '@angular/core';
import HistoricalHoldingValue from "../../dto/security/historical-holding-value";
import {SecurityHoldingFilter} from "../../dto/security/security-holding-filter";
import {SecurityApi} from "../../api/security-api";
import {Util} from "../../util";
import {SecurityUtil} from "../../security-util";
import {SecurityHoldingGraphDisplayField} from "../../dto/security/security-holding-graph-display-field";
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import * as echarts from "echarts/core";
import AggregatedHistoricalHoldingsValue from "../../dto/security/aggregated-historical-holdings-value";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-aggregated-security-holdings-graph',
  standalone: true,
  imports: [
    NgxEchartsDirective
  ],
  templateUrl: './aggregated-security-holdings-graph.component.html',
  styleUrl: './aggregated-security-holdings-graph.component.scss',
  providers: [
    provideEchartsCore({echarts}),
  ]
})
export class AggregatedSecurityHoldingsGraphComponent implements OnInit {

  chartOption: any;

  historicalValues: AggregatedHistoricalHoldingsValue[];

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

  initOptions: any = {};
  heightCss: string;


  constructor(private api: SecurityApi,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.initOptions = this.height == null ? {} : {'height': this.height};
      this.heightCss = this.height == null ? '' : 'height: ' + this.height + 'px;';

      this.resetGraph();
    });
  }

  resetGraph() {
    this.api.getAggregatedHistoricalValues().subscribe(historicalValues => {
      this.historicalValues = historicalValues;
      this.generateGraph();
    });
  }

  generateGraph() {
    let dates: string[] = [];
    let dividendValues: number[] = [];
    let taxValues: number[] = [];
    let adminCostValues: number[] = [];
    let worthValues: number[] = [];
    let positionValues: number[] = [];
    let holdingValuePerDate: any = {};
    let performanceValues: number[] = [];
    let totalCashWithdrawn: number[] = [];
    let totalCashInvested: number[] = [];

    for (let holdingValue of this.historicalValues) {
      let formattedDate = Util.formatDate(new Date(holdingValue.date), Util.DATE_FORMAT);
      dates.push(formattedDate);
      holdingValuePerDate[formattedDate] = holdingValue;

      dividendValues.push(holdingValue.totalDividend);
      taxValues.push(holdingValue.totalTaxes);
      adminCostValues.push(holdingValue.totalAdministrativeCosts);
      positionValues.push(holdingValue.positionValue);
      worthValues.push(holdingValue.worth);
      totalCashWithdrawn.push(holdingValue.totalCashWithdrawn);
      totalCashInvested.push(holdingValue.totalCashInvested);
      performanceValues.push(SecurityUtil.getPerformanceAggregated(holdingValue));
    }

    let series = [];


    series.push({
      data: positionValues,
      stack: 'y',
      name: SecurityHoldingGraphDisplayField.HOLDING_VALUE,
      areaStyle: {},
      type: 'line',
      showSymbol: false,
      color: Util.getColor(0),
      lineStyle: {color: Util.getColor(0)}
    });

    series.push({
      data: dividendValues,
      stack: 'y',
      name: SecurityHoldingGraphDisplayField.DIVIDEND,
      areaStyle: {},
      type: 'line',
      showSymbol: false,
      color: Util.getColor(1),
      lineStyle: {color: Util.getColor(1)}
    });

    series.push({
      data: taxValues,
      stack: 'y',
      name: SecurityHoldingGraphDisplayField.TAX,
      areaStyle: {},
      type: 'line',
      showSymbol: false,
      color: Util.getColor(2),
      lineStyle: {color: Util.getColor(2)}
    });

    series.push({
      data: adminCostValues,
      stack: 'y',
      name: SecurityHoldingGraphDisplayField.ADMIN_COST,
      areaStyle: {},
      type: 'line',
      showSymbol: false,
      color: Util.getColor(3),
      lineStyle: {color: Util.getColor(3)}
    });

    series.push({
      data: performanceValues,
      name: SecurityHoldingGraphDisplayField.PERFORMANCE,
      yAxisIndex: 1,
      type: 'line',
      showSymbol: false,
      color: Util.getColor(4),
      lineStyle: {color: Util.getColor(4)}
    });


    let tooltip = !this.showTooltip ? {} : this.getTooltip(holdingValuePerDate)
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
          name: 'money'
        },
        {
          type: 'value',
          position: 'right',
          name: 'percentage',
        },
        {
          type: 'value',
          position: 'right',
          name: 'price'
        }
      ],
      series: series,
      tooltip: tooltip,
      dataZoom: [
        {
          type: 'inside',
        },
      ],
    };

    if (this.showLegend) {
      options['legend'] = {
        position: "top",
        selected: {
          'DIVIDEND': false,
          'HOLDING_VALUE': false,
          'TAX': false,
          'ADMIN_COST': false
        },
      }
    }
    this.chartOption = options;
  }

  private showField(field: SecurityHoldingGraphDisplayField): boolean {
    return true;
  }


  private getTooltip(holdingValuePerDate: any) {
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
        let holdingValue = holdingValuePerDate[date];
        let currentHoldingValue = (holdingValue.price * holdingValue.amount);
        let performance = SecurityUtil.getPerformanceAggregated(holdingValue);

        function getMarker(params: any, field: SecurityHoldingGraphDisplayField) {
          for (let param of params) {
            if (param.seriesName == field) {
              return param.marker;
            }
          }
          return '';
        }


        return `${date}<br/>` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.HOLDING_VALUE)} Current holding value <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holdingValue.positionValue)};">${Util.currencyFormat(holdingValue.positionValue)}</span><br/>` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.ADMIN_COST)} Admin costs <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holdingValue.totalAdministrativeCosts)};">${Util.currencyFormat(holdingValue.totalAdministrativeCosts)}</span><br/>` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.TAX)} Taxes <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holdingValue.totalTaxes)};">${Util.currencyFormat(holdingValue.totalTaxes)}</span><br/>` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.MAX_CASH_INVESTED)} Invested <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holdingValue.maxCashInvested)};">${Util.currencyFormat(holdingValue.maxCashInvested)}</span><br/>` +
          `<hr style="color: grey;">` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.DIVIDEND)} Dividend <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holdingValue.totalDividend)};">${Util.currencyFormat(holdingValue.totalDividend)}</span><br/>` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.CASH_ON_HAND)} Cash on hand <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(holdingValue.cashOnHand)};">${Util.currencyFormat(holdingValue.cashOnHand)}</span><br/>` +
          `${getMarker(params, SecurityHoldingGraphDisplayField.PERFORMANCE)} Performance <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(performance)};">${Util.percentageFormat(performance)}</span><br/>`;


      },
    }
  }
}
