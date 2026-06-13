import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {SecurityHoldingGraphComponent} from "../security-holding-graph/security-holding-graph.component";
import {TranslatePipe} from "@ngx-translate/core";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Util} from "../../util";
import {SecurityTransactionListComponent} from "../security-transaction-list/security-transaction-list.component";
import {MatButton} from "@angular/material/button";
import {SecurityApi} from "../../api/security-api";

import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {MatFormField, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {SecurityHoldingFilter} from "../../dto/security/security-holding-filter";
import {provideNativeDateAdapter} from "@angular/material/core";
import {DialogService} from "../../service/dialog.service";
import {AuthService} from "../../service/auth.service";
import {MatCheckbox} from "@angular/material/checkbox";
import {PopoverComponent} from "../../components/popover/popover.component";
import {UserPreference} from "../../dto/user-preference";
import {UserPreferenceService} from "../../service/user-preference-service";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {Account} from "../../dto/account";
import {AccountApi} from "../../api/account-api";
import {Security} from "../../dto/security/security";

@Component({
    selector: 'app-security-holding-overview',
    imports: [
    MatTab,
    MatTabGroup,
    SecurityHoldingGraphComponent,
    SecurityTransactionListComponent,
    MatButton,
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatFormField,
    MatHint,
    MatInput,
    MatLabel,
    MatOption,
    MatSelect,
    MatSuffix,
    ReactiveFormsModule,
    MatCheckbox,
    PopoverComponent,
    MatRadioButton,
    MatRadioGroup,
    TranslatePipe
],
    templateUrl: './security-holding-overview.component.html',
    styleUrl: './security-holding-overview.component.scss',
    providers: [provideNativeDateAdapter()]
})
export class SecurityHoldingOverviewComponent implements OnInit {
  securityIds: string = '';
  startDate: string;
  securities: Security[] = [];
  accounts: Account[];

  form: FormGroup;
  chartLegendSelected: any;

  @Output()
  onFilterUpdateCallback: EventEmitter<SecurityHoldingFilter> = new EventEmitter();

  constructor(private route: ActivatedRoute,
              private router: Router,
              private dialogService: DialogService,
              private authService: AuthService,
              private accountApi: AccountApi,
              private api: SecurityApi) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady(() => {
      this.form = new FormGroup({
        accountIds: new FormControl(),
        securityIds: new FormControl(),
        dateRange: new FormControl(),
        startDate: new FormControl(),
        startAtZeroFromBeginning: new FormControl(),
        startAtZeroAfterDateAggregation: new FormControl(),
        aggregateDateRange: new FormControl(),
      });

      this.resetFilterFromUrl();
      this.reset();

      this.router.events.subscribe(event => {
        if (event instanceof NavigationEnd) {
          this.resetFilterFromUrl();
          this.reset();
        }
      });
    });
  }

  resetFilterFromUrl() {
    this.securityIds = Util.getUrlFragment('securityIds') as string;
    this.startDate = Util.getUrlFragment('startDate') as string;

    if (this.startDate != null) {
      this.form.controls['startDate'].setValue(this.startDate);
    } else {
      this.form.controls['startDate'].setValue(UserPreferenceService.get(UserPreference.SECURITY_START_DATE, null));
    }

    this.form.controls['securityIds'].setValue(this.securityIds == null ? [] : this.securityIds.split(',').map(v => parseInt(v)));
    this.form.controls['startAtZeroFromBeginning'].setValue(UserPreferenceService.get(UserPreference.SECURITY_START_AT_ZERO_FROM_BEGINNING, true) || this.form.value.nonGrouped);
    this.form.controls['startAtZeroAfterDateAggregation'].setValue(UserPreferenceService.get(UserPreference.SECURITY_START_AT_ZERO_AFTER_DATE_RANGE, true));
    this.form.controls['aggregateDateRange'].setValue(UserPreferenceService.get(UserPreference.SECURITY_AGGREGATE_DATE_RANGE, true));
    this.form.controls['dateRange'].setValue(UserPreferenceService.get(UserPreference.SECURITY_DATE_RANGE, null));
    this.form.controls['accountIds'].setValue(UserPreferenceService.get(UserPreference.SECURITY_ACCOUNT_IDS, '').toString().split(',').map(v => parseInt(v)));
  }

  onAccountIdsSelectChanged(value: any[]) {
    UserPreferenceService.onSelectChange(UserPreference.SECURITY_ACCOUNT_IDS, value);
  }

  resetUrlFromFilter() {
    let hash = '';
    if (this.form.value.securityIds != null) {
      hash += 'securityIds=' + this.form.value.securityIds + ';';
    }
    if (this.form.value.accountIds != null) {
      hash += 'accountIds=' + this.form.value.accountIds + ';';
    }
    if (this.form.value.startDate != null) {
      hash += 'startDate=' + Util.formatDate(this.form.value.startDate, 'yyyy-MM-dd') + ';';
    }
    if (this.chartLegendSelected != null) {
      hash += 'hiddenSeries=' + Object.keys(this.chartLegendSelected).filter(v => !this.chartLegendSelected[v]).join(',');
    }
    window.location.hash = hash;
  }

  reset() {
    this.accountApi.getAccounts().subscribe(accounts => {
      this.accounts = accounts;
      this.api.getSecurityHoldings().subscribe(holdings => {
        let processedSecurityIds: number[] = [];
        this.securities = [];
        holdings.map(holding => holding.security).forEach(security => {
          if(!processedSecurityIds.includes(security.id)) {
            this.securities.push(security);
            processedSecurityIds.push(security.id);
          }
        });
        if (this.securityIds != null) {
          let ids = this.securityIds.split(',').map(id => parseInt(id, 10));
          this.form.controls['securityIds'].setValue(ids);
          this.onUpdateFilterButtonClicked();
        }
      });
    });
  }

  onUpdateFilterButtonClicked() {
    this.onFilterUpdateCallback.next(this.getTransactionFilter());
  }

  getTransactionFilter() {
    let filter = new SecurityHoldingFilter();
    filter.accountIds = this.filterNonNull(this.form.value.accountIds);
    filter.securityIds = this.filterNonNull(this.form.value.securityIds);
    filter.dateRange = this.form.value.dateRange;
    filter.startDate = this.form.value.startDate;
    filter.startAtZeroAfterDateAggregation = this.form.value.startAtZeroAfterDateAggregation;
    filter.startAtZeroFromBeginning = this.form.value.startAtZeroFromBeginning;
    return filter;
  }

  onChartSeriesSelectedCallback(selected: any) {
    this.chartLegendSelected = selected.selected;
    this.resetUrlFromFilter();
  }

  getTitle() {
    if (this.securityIds != null) {
      let selectedIds: number[] = this.form.value.securityIds;
      return this.securities
        .filter(security => selectedIds.includes(security.id))
        .map(security => security.name)
        .join(', ');
    }
    return '';
  }


  onAggregateDateRangeChanged(checked: boolean) {
    if (this.form.value.aggregateDateRange) {
      if (this.form.value.nonGrouped) {
        this.form.controls['startAtZeroFromBeginning'].setValue(true);
        UserPreferenceService.set(UserPreference.SECURITY_START_AT_ZERO_FROM_BEGINNING, true);
      }
    } else {
      this.UserPreferenceService.set(UserPreference.SECURITY_DATE_RANGE, null);
      this.form.controls['dateRange'].setValue(null);
    }
    UserPreferenceService.onCheckboxChange(UserPreference.SECURITY_AGGREGATE_DATE_RANGE, checked);
  }

  filterNonNull(array: any) {
    if(array == null) {
      return null;
    }
    return array.filter((entry: any) => entry != null && !isNaN(entry))
  }

  protected readonly Util = Util;
  protected readonly UserPreference = UserPreference;
  protected readonly UserPreferenceService = UserPreferenceService;
}
