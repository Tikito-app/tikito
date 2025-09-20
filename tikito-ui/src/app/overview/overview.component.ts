import {Component, OnInit} from '@angular/core';
import {OverviewApi} from "../api/overview-api";
import {SecurityApi} from "../api/security-api";
import {AuthService} from "../service/auth.service";
import {Overview} from "../dto/overview";
import {MatGridList, MatGridTile} from "@angular/material/grid-list";
import AggregatedHistoricalHoldingsValue from "../dto/security/aggregated-historical-holdings-value";
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import {Util} from "../util";
import {SecurityHoldingGraphDisplayField} from "../dto/security/security-holding-graph-display-field";
import * as echarts from "echarts/core";
import {MoneyApi} from "../api/money-api";
import {AggregatedHistoricalMoneyHoldingValue} from "../dto/money/aggregated-historical-money-holding-value";
import moment from "moment/moment";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [
    MatGridList,
    MatGridTile,
    NgxEchartsDirective,
    FormsModule,
    NgIf,
    ReactiveFormsModule,
    TranslatePipe
  ],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
  providers: [
    provideEchartsCore({echarts}),
  ]
})
export class OverviewComponent implements OnInit {
  overview: Overview;

  aggregatedHistoricalSecurityValues: AggregatedHistoricalHoldingsValue[];
  aggregatedHistoricalMoneyHoldingValues: AggregatedHistoricalMoneyHoldingValue[] = [];
  chartOption: any;
  initOptions: any = {};
  heightCss: string;

  constructor(private api: OverviewApi,
              private securityApi: SecurityApi,
              private moneyApi: MoneyApi,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.reset();
    });
  }

  reset(): void {
    this.securityApi.getAggregatedHistoricalValues().subscribe(historicalSecurityValues => {
      this.aggregatedHistoricalSecurityValues = historicalSecurityValues;
      this.moneyApi.getAggregatedHistoricalMoneyHoldingValues().subscribe(historicalMoneyHoldings => {
        this.aggregatedHistoricalMoneyHoldingValues = historicalMoneyHoldings;
        if (historicalMoneyHoldings.length > 0) {
          this.interpolateHistoricalSecurityOrMoneyValues();
          this.generateGraph();
        }
      });
    });

    this.api.getOverview().subscribe(overview => this.overview = overview);
  }

  interpolateHistoricalSecurityOrMoneyValues() {
    let moneyStartDate = moment(this.aggregatedHistoricalMoneyHoldingValues[0].date);
    let moneyEndDate = moment(this.aggregatedHistoricalMoneyHoldingValues.slice(-1)[0].date);
    let securityStartDate = moment(this.aggregatedHistoricalSecurityValues[0].date);
    let securityEndDate = moment(this.aggregatedHistoricalMoneyHoldingValues.slice(-1)[0].date);

    if (moneyStartDate.isBefore(securityStartDate)) {
      this.aggregatedHistoricalSecurityValues = this.interpolateValuesStartOf(moneyStartDate, this.aggregatedHistoricalSecurityValues, {positionValue: 0});
    } else {
      this.aggregatedHistoricalMoneyHoldingValues = this.interpolateValuesStartOf(securityStartDate, this.aggregatedHistoricalMoneyHoldingValues, {amount: 0});
    }
    if (moneyEndDate.isBefore(securityEndDate)) {
      this.aggregatedHistoricalMoneyHoldingValues = this.interpolateValuesEndOf(securityEndDate, this.aggregatedHistoricalMoneyHoldingValues, {amount: 0});
    } else {
      this.aggregatedHistoricalSecurityValues = this.interpolateValuesEndOf(moneyStartDate, this.aggregatedHistoricalSecurityValues, {positionValue: 0});
    }
  }

  interpolateValuesStartOf(startDate: moment.Moment, values: any[], dto: any): any[] {
    let endDate = moment(values[0].date);
    while (startDate.isBefore(endDate)) {
      let newDto = dto;
      newDto.date = startDate.format('YYYY-MM-DD');
      values.unshift(newDto);
      startDate = startDate.add(1, 'day');
    }
    return values;
  }

  interpolateValuesEndOf(endDate: moment.Moment, values: any[], dto: any): any[] {
    let startDate = moment(values.slice(-1)[0].date);
    while (startDate.isBefore(endDate)) {
      let newDto = dto;
      newDto.date = startDate.format('YYYY-MM-DD');
      values.push(newDto);
      startDate = startDate.add(1, 'day');
    }
    return values;
  }

  generateGraph() {
    let dates: string[] = [];
    let moneyHoldingValuePerDate: any = {};
    let securityHoldingValuePerDate: any = {};
    let positionValues: number[] = [];
    let moneyPositionValues: number[] = [];

    for (let holdingValue of this.aggregatedHistoricalSecurityValues) {
      let formattedDate = Util.formatDate(new Date(holdingValue.date), Util.DATE_FORMAT);
      // dates.push(formattedDate);
      securityHoldingValuePerDate[formattedDate] = holdingValue;
      positionValues.push(holdingValue.positionValue);
    }

    for (let holdingValue of this.aggregatedHistoricalMoneyHoldingValues) {
      let formattedDate = Util.formatDate(new Date(holdingValue.date), Util.DATE_FORMAT);
      dates.push(formattedDate);
      moneyHoldingValuePerDate[formattedDate] = holdingValue;
      moneyPositionValues.push(holdingValue.amount);
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
      data: moneyPositionValues,
      stack: 'y',
      name: SecurityHoldingGraphDisplayField.WORTH,
      areaStyle: {},
      type: 'line',
      showSymbol: false,
      color: Util.getColor(1),
      lineStyle: {color: Util.getColor(1)}
    });

    let options: any = {
      title: {
        text: 'All assets'
      },
      xAxis: {
        type: 'category',
        data: dates
      },
      yAxis: [
        {
          type: 'value',
          name: 'euro'
        },
      ],
      series: series,
      tooltip: this.getTooltip(securityHoldingValuePerDate, moneyHoldingValuePerDate),
      dataZoom: [
        {
          type: 'inside',
        },
      ],
    };

    this.chartOption = options;
  }


  private getTooltip(holdingValuePerDate: any, moneyHoldingValuePerDate: any) {
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
        let securityValue = holdingValuePerDate[date];
        let moneyValue = moneyHoldingValuePerDate[date];

        function getMarker(params: any, field: SecurityHoldingGraphDisplayField) {
          for (let param of params) {
            if (param.seriesName == field) {
              return param.marker;
            }
          }
          return '';
        }

        let securityTooltip = '';
        let moneyTooltip = '';
        if (securityValue != null) {
          securityTooltip = `${getMarker(params, SecurityHoldingGraphDisplayField.HOLDING_VALUE)} Security <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(securityValue.positionValue)};">${Util.currencyFormatWithSymbol(securityValue.positionValue, 47)}</span><br/>`;
        }
        if (moneyValue != null) {
          moneyTooltip = `${getMarker(params, SecurityHoldingGraphDisplayField.WORTH)} Money <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(moneyValue.amount)};">${Util.currencyFormatWithSymbol(moneyValue.amount, 47)}</span>`;
        }

        return `${date}<br/>` + securityTooltip + moneyTooltip;
      },
    }
  }
}
