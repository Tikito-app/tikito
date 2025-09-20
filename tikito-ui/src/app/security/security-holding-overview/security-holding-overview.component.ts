import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {SecurityHoldingGraphComponent} from "../security-holding-graph/security-holding-graph.component";
import {TranslatePipe} from "@ngx-translate/core";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Util} from "../../util";
import {SecurityTransactionListComponent} from "../security-transaction-list/security-transaction-list.component";
import {MatButton} from "@angular/material/button";
import {SecurityApi} from "../../api/security-api";
import SecurityHolding from "../../dto/security/security-holding";
import {NgIf} from "@angular/common";
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

@Component({
  selector: 'app-security-holding-overview',
  standalone: true,
  imports: [
    MatTab,
    MatTabGroup,
    SecurityHoldingGraphComponent,
    TranslatePipe,
    SecurityTransactionListComponent,
    NgIf,
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
    MatRadioGroup
  ],
  templateUrl: './security-holding-overview.component.html',
  styleUrl: './security-holding-overview.component.scss',
  providers: [provideNativeDateAdapter(), TranslatePipe],
})
export class SecurityHoldingOverviewComponent implements OnInit {
  holdingIds: string = '';
  startDate: string;
  holdings: SecurityHolding[] = [];

  form: FormGroup;
  chartLegendSelected: any;

  @Output()
  onFilterUpdateCallback: EventEmitter<SecurityHoldingFilter> = new EventEmitter();

  constructor(private route: ActivatedRoute,
              private router: Router,
              private dialogService: DialogService,
              private translate: TranslatePipe,
              private authService: AuthService,
              private api: SecurityApi) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.form = new FormGroup({
        holdingIds: new FormControl(),
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
    this.holdingIds = Util.getUrlFragment('holdingIds') as string;
    this.startDate = Util.getUrlFragment('startDate') as string;

    this.form.controls['holdingIds'].setValue(this.holdingIds == null ? [] : this.holdingIds.split(',').map(v => parseInt(v)));
    if (this.startDate != null) {
      this.form.controls['startDate'].setValue(this.startDate);
    }

    this.form.controls['startAtZeroFromBeginning'].setValue(UserPreferenceService.get(UserPreference.SECURITY_START_AT_ZERO_FROM_BEGINNING, true) || this.form.value.nonGrouped);
    this.form.controls['startAtZeroAfterDateAggregation'].setValue(UserPreferenceService.get(UserPreference.SECURITY_START_AT_ZERO_AFTER_DATE_RANGE, true));
    this.form.controls['aggregateDateRange'].setValue(UserPreferenceService.get(UserPreference.SECURITY_AGGREGATE_DATE_RANGE, true));
    this.form.controls['dateRange'].setValue(UserPreferenceService.get(UserPreference.SECURITY_DATE_RANGE, null));
  }

  resetUrlFromFilter() {
    let hash = '';
    if (this.form.value.holdingIds != null) {
      hash += 'holdingIds=' + this.form.value.holdingIds + ';';
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
    this.api.getSecurityHoldings().subscribe(holdings => {
      this.holdings = holdings;
      if (this.holdingIds != null) {
        let ids = this.holdingIds.split(',').map(id => parseInt(id, 10));
        this.form.controls['holdingIds'].setValue(ids);
        this.onUpdateFilterButtonClicked();
      }
    });
  }

  onUpdateFilterButtonClicked() {
    this.onFilterUpdateCallback.next(this.getTransactionFilter());
  }

  getTransactionFilter() {
    let filter = new SecurityHoldingFilter();
    // if(this.form != null) {}
    filter.holdingIds = this.form.value.holdingIds;
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
    if (this.holdingIds != null) {
      let selectedIds: number[] = this.form.value.holdingIds;
      return this.holdings
        .filter(holding => selectedIds.includes(holding.id))
        .map(holding => holding.security.name)
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

  protected readonly Util = Util;
  protected readonly UserPreference = UserPreference;
  protected readonly UserPreferenceService = UserPreferenceService;
}
