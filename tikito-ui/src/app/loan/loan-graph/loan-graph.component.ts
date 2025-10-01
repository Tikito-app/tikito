import {Component, Input, OnInit} from '@angular/core';
import {LoanApi} from "../../api/loan-api";
import {AuthService} from "../../service/auth.service";
import {LoanValue} from "../../dto/loan-value";
import {Loan} from "../../dto/loan";
import {Util} from "../../util";
import moment from "moment";
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import * as echarts from "echarts/core";

@Component({
  selector: 'app-loan-graph',
  standalone: true,
  imports: [
    NgxEchartsDirective
  ],
  providers: [
    provideEchartsCore({echarts}),
  ],
  templateUrl: './loan-graph.component.html',
  styleUrl: './loan-graph.component.scss'
})
export class LoanGraphComponent implements OnInit {
  @Input()
  loanValues: LoanValue[];

  @Input()
  loans: Loan[];

  @Input()
  height: number;
  chartOption: any;

  constructor(private api: LoanApi,
              private authService: AuthService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      setTimeout(() => {
        this.resetGraph();
      }, 500);
    });
  }

  resetGraph() {
    let firstDate = this.loanValues[0].date;
    let lastDate = this.loanValues[this.loanValues.length - 1].date;
    let dateRangeFormat = this.getDateRangeFormat();
    let firstDateToRender = moment(firstDate);
    let lastDateToRender = moment(lastDate).add(2, 'month');
    let allDates: string[] = [];
    let allDatesSimulated: string[] = [];
    let currentDate = moment(firstDateToRender).add(1, 'day');
    let seriesValuesByName: any = {};
    let loanValuesPerDate: any = {};
    let loanPartsById: any = {};
    let simulatedStartIndex = -1;

    this.loans.forEach((loan: Loan) => {
      seriesValuesByName[loan.name + ' interest'] = [];
      seriesValuesByName[loan.name + ' interest-simulated'] = [];
      seriesValuesByName[loan.name + ' repayment'] = [];
      seriesValuesByName[loan.name + ' repayment-simulated'] = [];
      loan.loanParts.forEach(part => {
        loanPartsById[part.id] = part;
      })
    });

    this.loanValues.forEach((loanValue: LoanValue) => {
      let date = moment(loanValue.date).format('DD-MM-yyyy');
      if (loanValuesPerDate[date] == null) {
        loanValuesPerDate[date] = [];
      }
      loanValuesPerDate[date].push(loanValue);
    });

    while (currentDate.isSameOrBefore(lastDateToRender)) {
      let yesterdayFormated = moment(currentDate).subtract(1, 'day').format('DD-MM-yyyy');
      let currentDateFormated = currentDate.format('DD-MM-yyyy');
      let currentRangedString = currentDate.format(dateRangeFormat);
      let withinDateRange = currentDate.isSameOrAfter(firstDateToRender);

      if (!withinDateRange) {
        continue;
      }


      let simulated = false;
      this.loans.forEach((loan: Loan) => {
        let interest = 0;
        let repayment = 0;
        if (loanValuesPerDate[yesterdayFormated] != null) {
          loanValuesPerDate[yesterdayFormated].forEach((loanValue: LoanValue) => {
            interest += loanValue.interestPaid;
            repayment += loanValue.loanPaid;
            simulated = simulated || loanValue.simulated;
          });
        }

        if(!simulated) {
          seriesValuesByName[loan.name + ' interest'].push(interest);
          seriesValuesByName[loan.name + ' interest-simulated'].push(null);
          seriesValuesByName[loan.name + ' repayment'].push(repayment);
          seriesValuesByName[loan.name + ' repayment-simulated'].push(null);
        } else {
          seriesValuesByName[loan.name + ' interest'].push(null);
          seriesValuesByName[loan.name + ' interest-simulated'].push(interest);
          seriesValuesByName[loan.name + ' repayment'].push(null);
          seriesValuesByName[loan.name + ' repayment-simulated'].push(repayment);
        }
      });

      allDates.push(currentDateFormated);

      let nextDateRangeString = currentRangedString;
      while (nextDateRangeString == currentRangedString) {
        currentDate = currentDate.add(1, 'day');
        nextDateRangeString = currentDate.format(dateRangeFormat);
      }
    }

    let seriesWithGroups: any = this.generateSeriesGroups(seriesValuesByName);
    this.chartOption = this.generateGraphOptions(allDates, seriesWithGroups, loanValuesPerDate, loanPartsById, simulatedStartIndex);
  }


  generateSeriesGroups(seriesValuesByName: any) {
    return Object.keys(seriesValuesByName)
      .map((name: any) => {
        let group: any = {
          data: seriesValuesByName[name],
          name: name,
          type: 'line',
          stack: true,
          showSymbol: false,
          lineStyle: {
            type: name.endsWith('simulated') ? 'dashed' : 'solid',
            width: 1
          },
          color: name.endsWith('interest') || name.endsWith('interest-simulated') ? Util.COLORS[1] : Util.COLORS[0],
          areaStyle: {
            color: name.endsWith('interest') || name.endsWith('interest-simulated') ? Util.COLORS[1] : Util.COLORS[0],
            opacity: name.endsWith('simulated') ? 0.1 : 1
          }
        };
        return group;
      })
  }

  generateGraphOptions(allDates: string[], seriesWithGroups: any, loanValuesPerDate: any, loanPartsById: any, simulatedStartIndex: number) {
    let options: any = {
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
      tooltip: this.getTooltip(loanValuesPerDate, loanPartsById),
      legend: {
        position: "top",
      },
      dataZoom: [{
        type: 'slider',
      }],
    };

    // if(simulatedStartIndex != -1) {
    //   options.visualMap = {
    //     show: true,
    //     pieces: [
    //       {
    //         lte: simulatedStartIndex,
    //         color: 'green'
    //       },
    //       {
    //         gt: simulatedStartIndex,
    //         color: 'red'
    //       }
    //     ]
    //   };
    // }

    return options;
  }

  getTooltip(loanValuesPerDate: any, loanPartsById: any) {
    return {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter(params: any): any {
        let date = moment(params[0].axisValue, 'DD-MM-yyyy').subtract(1, 'day').format('DD-MM-yyyy');
        let html = '';
        let values: any = loanValuesPerDate[date];
        if(values == null) {
          return '';
        }

        let param = params[0];
        if (param.value != 0) {
          let totalInterestPaid = 0;
          let totalInterestPaidThisPeriod = 0;
          let totalRepayment = 0;
          let totalRepaymentThisPeriod = 0;

          values.forEach((value: any) => {
            totalRepayment += value.loanPaid;
            totalRepaymentThisPeriod += value.loanPaidThisPeriod;
            totalInterestPaid += value.interestPaid;
            totalInterestPaidThisPeriod += value.interestPaidThisPeriod;
          });

          html += '<table>';
          html += '<tr><td></td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px;">${loanPartsById[value.loanPartId].name}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Interest paid</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(value.interestPaid)};">${Util.currencyFormat(value.interestPaid)}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Interest paid this period</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(-value.interestPaidThisPeriod)};">${Util.currencyFormat(value.interestPaidThisPeriod)}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Interest remaining</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(-value.interestRemaining)};">${Util.currencyFormat(value.interestRemaining)}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Amount remaining</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(-value.amountRemaining)};">${Util.currencyFormat(value.amountRemaining)}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Loan paid</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(value.loanPaid)};">${Util.currencyFormat(value.loanPaid)}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Loan paid this period</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(value.loanPaidThisPeriod)};">${Util.currencyFormat(value.loanPaidThisPeriod)}</span></td>`).join('') + '<tr>';
          html += '<tr><td>Loan behind</td>' + Object.values(values).map((value: any) => `<td><span style="float: right; margin-left: 20px; color: ${Util.currencyColor(-value.repaymentRemaining)};">${Util.currencyFormat(value.repaymentRemaining)}</span></td>`).join('') + '<tr>';
          html += '</table>';
          html += '<hr>';
          html += `Repaid this period <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(totalRepaymentThisPeriod)};">${Util.currencyFormat(totalRepaymentThisPeriod)}</span><br/>`;
          html += `Interest paid this period <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(totalInterestPaidThisPeriod)};">${Util.currencyFormat(totalInterestPaidThisPeriod)}</span><br/>`;
          html += `Repaid <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(totalRepayment)};">${Util.currencyFormat(totalRepayment)}</span><br/>`;
          html += `Total interest paid <span style="float: right; margin-left: 20px; color: ${Util.currencyColor(totalInterestPaid)};">${Util.currencyFormat(totalInterestPaid)}</span><br/>`;
        }

        return `${date}<br/>` + html;
      },
    }
  }

  getDateRangeFormat() {
    // let range = this.transactionFilter.dateRange;
    // if (range == TransactionDateRange.WEEK) {
    //   return 'YYYY-WW';
    // } else if (range == TransactionDateRange.MONTH) {
    return 'YYYY-MM';
    // } else if (range == TransactionDateRange.YEAR) {
    //   return 'YYYY';
    // }

    // return 'YYYY-MM-DD';
  }
}
