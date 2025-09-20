import {Component, Input, OnInit} from '@angular/core';
import {NgxEchartsDirective, provideEchartsCore} from "ngx-echarts";
import {SecurityApi} from "../../api/security-api";
import * as echarts from "echarts/core";
import SecurityHolding from "../../dto/security/security-holding";
import {SecurityHoldingValue} from "../../dto/security/security-holding-value";
import {AuthService} from "../../service/auth.service";
import {CacheService} from "../../service/cache-service";
import {Util} from "../../util";

@Component({
  selector: 'app-trading-company-pie-graph',
  standalone: true,
    imports: [
        NgxEchartsDirective
    ],
  templateUrl: './security-pie-graph.component.html',
  styleUrl: './security-pie-graph.component.scss',
  providers: [
    provideEchartsCore({echarts}),
  ]
})
export class SecurityPieGraphComponent implements OnInit {

  chartOption: any;
  initOptions: any = {};
  heightCss: string;
  securityHoldings: SecurityHolding[];

  @Input()
  height: number;

  @Input()
  valueType: string;

  constructor(private api: SecurityApi,
              private authService: AuthService,) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.initOptions = this.height == null ? {} : {'height': this.height};
      this.heightCss = this.height == null ? '' : 'height: ' + this.height + 'px;';
      this.resetGraph();
    });
  }

  resetGraph() {
    this.api.getSecurityHoldings().subscribe(securityHoldings => {
      this.securityHoldings = securityHoldings.filter(holding => holding.amount > 0);
      this.generateGraph();
    });
  }

  generateGraph() {
    this.chartOption = {
      tooltip: {
        trigger: 'item',
        formatter: (params: any) =>  {
          return `${params.seriesName}<br>${params.marker}${params.name}<span style="float: right; margin-left: 20px"><b>${params.value}%</b></span>`;
        }
      },
      legend: {
        orient: 'vertical',
        right: 10,
      },
      series: [
        {
          name: 'Sector',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: true,
          labelLine: {
            show: false
          },
          data: this.generateData(),
          color: Util.COLORS
        }
      ]
    }
  }

  generateData() {
    let total = this.securityHoldings
      .map(holding => this.getHoldingValue(holding))
      .reduce((sum, current) => sum + current, 0);

    let valuePerType: any = {};
    this.securityHoldings.forEach(holding => {
      let key: any = this.getKey(holding)

      if(valuePerType[key] == null) {
        valuePerType[key] = 0;
      }
      valuePerType[key] += this.getHoldingValue(holding);
    });

    return Object.keys(valuePerType)
      .map(key => {
        return {
          value: total == 0 ? 0 : Math.round(valuePerType[key] / total * 100),
          name: key == 'null' ? 'Unknown' : key
        }});
  }

  getHoldingValue(holding: SecurityHoldingValue) {
    return holding.price * holding.amount;
  }

  getKey(holding: SecurityHolding): string {
    if(this.valueType == 'sector') {
      return holding.security.sector
    } else if(this.valueType == 'industry') {
      return holding.security.industry
    } else if(this.valueType == 'currency') {
      return "" + CacheService.getCurrencyById(holding.security.currencyId).name;
    } else if(this.valueType == 'security-type') {
      return holding.security.securityType;
    }
    return 'unknown';
  }
}
