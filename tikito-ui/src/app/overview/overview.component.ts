import {Component, OnInit} from '@angular/core';
import {OverviewApi} from "../api/overview-api";
import {SecurityApi} from "../api/security-api";
import {AuthService} from "../service/auth.service";
import {Overview} from "../dto/overview";
import AggregatedHistoricalHoldingsValue from "../dto/security/aggregated-historical-holdings-value";
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import {Util} from "../util";
import * as echarts from "echarts/core";
import {MoneyApi} from "../api/money-api";
import {AggregatedHistoricalMoneyHoldingValue} from "../dto/money/aggregated-historical-money-holding-value";
import moment from "moment/moment";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

import {Moment} from "moment";
import {LoanApi} from "../api/loan-api";
import {OverviewLoanComponent} from "./overview-loan/overview-loan.component";
import {LoanValue} from "../dto/loan-value";
import {TranslatePipe, TranslateService} from "@ngx-translate/core";
import {MoneyHolding} from "../dto/money-holding";
import {CurrencyComponent} from "../components/currency/currency.component";
import {AssetType} from "../dto/asset-type";

@Component({
    selector: 'app-overview',
    imports: [
    NgxEchartsDirective,
    FormsModule,
    ReactiveFormsModule,
    OverviewLoanComponent,
    TranslatePipe,
    CurrencyComponent
],
    templateUrl: './overview.component.html',
    styleUrl: './overview.component.scss',
    providers: [
        provideEchartsCore({ echarts }),
    ]
})
export class OverviewComponent implements OnInit {
  overview: Overview;

  aggregatedValuesPerAssetType: any = {};

  aggregatedHistoricalSecurityValues: AggregatedHistoricalHoldingsValue[];
  aggregatedHistoricalMoneyHoldingValues: AggregatedHistoricalMoneyHoldingValue[] = [];
  chartOption: any;
  initOptions: any = {};
  loansById: any = {};
  loanValuesPerLoanId: any = {};
  holdings: MoneyHolding[];

  constructor(private api: OverviewApi,
              private securityApi: SecurityApi,
              private moneyApi: MoneyApi,
              private loanApi: LoanApi,
              private translateService: TranslateService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.reset();
    });
  }

  reset(): void {
    this.securityApi.getAggregatedHistoricalValues().subscribe(historicalSecurityValues => {
      this.aggregatedValuesPerAssetType[AssetType.SECURITY] = historicalSecurityValues;

      this.aggregatedHistoricalSecurityValues = historicalSecurityValues;
      this.moneyApi.getAggregatedHistoricalMoneyHoldingValues().subscribe(historicalMoneyHoldings => {
        historicalMoneyHoldings.forEach(value => {
          if (this.aggregatedValuesPerAssetType[value.moneyType] == null) {
            this.aggregatedValuesPerAssetType[value.moneyType] = [];
          }
          this.aggregatedValuesPerAssetType[value.moneyType].push(value);
        })

        this.interpolateValues();
        this.generateGraph();
      });
    });

    this.loanApi.getLoans().subscribe(loans => {
      loans.forEach(loan => this.loansById[loan.id] = loan);
      this.loanApi.getLoanValuesForCurrentDate().subscribe(values => {
        this.processLoanValues(values);
      });
    });

    this.moneyApi.getHoldings().subscribe(holdings => this.holdings = holdings);
    this.api.getOverview().subscribe(overview => this.overview = overview);
  }

  processLoanValues(values: LoanValue[]) {
    values.forEach(value => {
      if (this.loanValuesPerLoanId[value.loanId] == null) {
        this.loanValuesPerLoanId[value.loanId] = value;
      } else {
        this.loanValuesPerLoanId[value.loanId].amountRemaining += value.amountRemaining;
        this.loanValuesPerLoanId[value.loanId].interestRemaining += value.interestRemaining;
        this.loanValuesPerLoanId[value.loanId].loanPaid += value.loanPaid;
        this.loanValuesPerLoanId[value.loanId].interestPaid += value.interestPaid;
      }
    });
  }

  interpolateValues() {
    let startDate: Moment | null = null;
    let endDate: Moment | null = null;

    for (let type of Object.keys(this.aggregatedValuesPerAssetType)) {
      let assetStartDate = moment(this.aggregatedValuesPerAssetType[type][0].date);
      let assetEndDate = moment(this.aggregatedValuesPerAssetType[type][this.aggregatedValuesPerAssetType[type].length - 1].date);
      if (startDate == null || assetStartDate.isBefore(startDate)) {
        startDate = assetStartDate;
      }
      if (endDate == null || assetEndDate.isAfter(endDate)) {
        endDate = assetEndDate;
      }
    }

    for (let type of Object.keys(this.aggregatedValuesPerAssetType)) {
      this.aggregatedValuesPerAssetType[type] = this.interpolateValuesStartOf(startDate as Moment, this.aggregatedValuesPerAssetType[type]);
      this.aggregatedValuesPerAssetType[type] = this.interpolateValuesEndOf(endDate as Moment, this.aggregatedValuesPerAssetType[type]);
    }
  }

  interpolateValuesStartOf(initialDate: moment.Moment, values: any[]): any[] {
    let endDate = moment(values[0].date);
    let newValues: any[] = [];
    let startDate = initialDate.clone();
    while (startDate.isBefore(endDate)) {
      let newDto: any = {positionValue: 0, amount: 0, date: null}
      newDto.date = startDate.format('YYYY-MM-DD');
      startDate = startDate.add(1, 'day');
      newValues.push(newDto);
    }
    return newValues.concat(values);
  }

  interpolateValuesEndOf(initialDate: moment.Moment, values: any[]): any[] {
    let currentDate = moment(values.slice(-1)[0].date);
    let endDate = initialDate.clone();
    while (currentDate.isBefore(endDate)) {
      let newDto: any = {positionValue: 0, amount: 0, date: null}
      newDto.date = currentDate.format('YYYY-MM-DD');
      values.push(newDto);
      currentDate = currentDate.add(1, 'day');
    }
    return values;
  }

  generateGraph() {
    let dates: string[] = [];
    let series = [];
    let processedDates = false;
    let valuesPerTypeAndDate: any = {}

    for (let type of Object.keys(this.aggregatedValuesPerAssetType)) {
      let values = [];
      valuesPerTypeAndDate[type] = {};

      for (let value of this.aggregatedValuesPerAssetType[type]) {
        let currentDate = new Date(value.date);
        let formattedDate = Util.formatDate(currentDate, Util.DATE_FORMAT);

        if (!processedDates) {
          dates.push(formattedDate);
        }

        values.push(value.amount ? value.amount : value.positionValue);
        valuesPerTypeAndDate[type][formattedDate] = value;
      }
      processedDates = true;

      series.push({
        data: values,
        stack: 'y',
        name: type,
        areaStyle: {},
        type: 'line',
        showSymbol: false,
        color: Util.getColor(series.length),
        lineStyle: {color: Util.getColor(series.length)}
      })
    }

    this.chartOption = {
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
      tooltip: this.getTooltip(valuesPerTypeAndDate, this.translateService),
      dataZoom: [
        {
          type: 'inside',
        },
      ],
    };
  }


  private getTooltip(valuesPerTypeAndDate: any, translateService: TranslateService) {
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

        function getMarker(params: any, field: string) {
          for (let param of params) {
            if (param.seriesName == field) {
              return param.marker;
            }
          }
          return '';
        }

        let html = '';
        let total = 0;

        for (let type of Object.keys(valuesPerTypeAndDate)) {
          let asset = valuesPerTypeAndDate[type][date];
          let value = asset.amount ? asset.amount : asset.positionValue;

          if (value != null && value != 0) {
            html += `${getMarker(params, type)} ${translateService.instant('money/type/' + type)} <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(value)};">${Util.currencyFormatWithSymbol(value, 47)}</span><br/>`;
            total += value;
          }
        }

        html += `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;"></span> ${translateService.instant('total')} <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(total)};">${Util.currencyFormatWithSymbol(total, 47)}</span><br/>`;

        return `${date}<br/>` + html;
      },
    }
  }

  protected readonly Object = Object;
}
