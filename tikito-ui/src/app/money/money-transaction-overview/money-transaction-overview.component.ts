import {Component, computed, EventEmitter, model, OnInit, Output, signal} from '@angular/core';
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {TranslatePipe} from "../../service/translate-pipe.pipe";
import {MoneyTransactionListComponent} from "../money-transaction-list/money-transaction-list.component";
import {ActivatedRoute, Router} from "@angular/router";
import {Util} from "../../util";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatCheckbox, MatCheckboxChange} from "@angular/material/checkbox";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatOption, MatSelect} from "@angular/material/select";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {MoneyApi} from "../../api/money-api";
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerModule,
  MatDatepickerToggle
} from "@angular/material/datepicker";
import {MatButton} from "@angular/material/button";
import {MAT_DATE_LOCALE, provideNativeDateAdapter} from "@angular/material/core";
import {MatInput, MatInputModule} from "@angular/material/input";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {MoneyTransactionsFilter, TransactionDateRange} from "../../dto/money/money-transactions-filter";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {MatIcon, MatIconModule} from "@angular/material/icon";
import {PopoverModule} from "../../components/popover/popover.module";
import {PopoverComponent} from "../../components/popover/popover.component";
import {NgIf} from "@angular/common";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {MatChipsModule} from "@angular/material/chips";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {AuthService} from "../../service/auth.service";
import {Security} from "../../dto/security/security";
import {CacheService} from "../../service/cache-service";
import {MoneyGraphComponent} from "../money-graph/money-graph.component";
import moment from "moment";

@Component({
  selector: 'app-money-transaction-overview',
  standalone: true,
  imports: [
    MatTabGroup,
    MatTab,
    MoneyTransactionListComponent,
    MatCard,
    MatCardTitle,
    MatCardContent,
    MatCardHeader,
    MatCheckbox,
    MatDatepickerModule,
    MatFormField,
    MatInputModule,
    MatLabel,
    MatOption,
    MatSelect,
    ReactiveFormsModule,
    MatDatepickerToggle,
    MatButton,
    MatInput,
    MatDatepickerInput,
    MatDatepicker,
    MatRadioGroup,
    MatRadioButton,
    MatIcon,
    PopoverModule,
    MatIconModule,
    MatChipsModule,
    PopoverComponent,
    NgIf,
    FormsModule,
    TranslatePipe,
    MoneyGraphComponent
  ],
  templateUrl: './money-transaction-overview.component.html',
  styleUrl: './money-transaction-overview.component.scss',
  providers: [provideNativeDateAdapter(), {provide: MAT_DATE_LOCALE, useValue: 'nl-NL'}],
})
export class MoneyTransactionOverviewComponent implements OnInit {
  accountId: number;
  groups: MoneyTransactionGroup[];
  accounts: Account[];
  currencies: Security;

  form: FormGroup;
  includeBudgetDisabled: boolean = false;

  readonly currentGroup = model('');
  readonly selectedGroups = signal(['Lemon']);
  allGroups: string[] = [];
  filteredGroups: any;

  @Output()
  onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter> = new EventEmitter();

  constructor(private route: ActivatedRoute,
              private router: Router,
              private api: MoneyApi,
              private authService: AuthService,
              private accountApi: AccountApi) {
    this.accountId = Util.getIdFromRoute(route, 'accountId') as number;
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.form = new FormGroup({
        accountIds: new FormControl(),
        currencies: new FormControl(),
        groupIds: new FormControl(),
        startAtZeroFromBeginning: new FormControl(),
        startAtZeroAfterDateAggregation: new FormControl(),
        showOther: new FormControl(),
        aggregateDateRange: new FormControl(),
        nonGrouped: new FormControl(),
        dateRange: new FormControl(),
        amountOfOtherGroups: new FormControl(),
        startDate: new FormControl,
        endDate: new FormControl,
        includeBudget: new FormControl(),
        includeMoney: new FormControl(),
        includeMoneyHolding: new FormControl(),
        transactionFilter: new FormControl()
      });
      this.reset();
    });
  }

  reset(): void {
    let filter = new MoneyTransactionsFilter();
    filter.groupIds = this.form.value.groupIds == null ? [] : this.form.value.groupIds.filter((id: any) => id != null);
    filter.dateRange = this.form.value.dateRange;

    this.accountApi.getAccounts().subscribe(accounts => {
      this.accounts = accounts;
      this.api.getMoneyTransactionGroups().subscribe(groups => {
        this.groups = groups;
        this.allGroups = this.groups.map(group => group.name);
        this.filteredGroups = computed(() => {
          const currentFruit = this.currentGroup().toLowerCase();
          return currentFruit
            ? this.allGroups.filter(fruit => fruit.toLowerCase().includes(currentFruit))
            : this.allGroups.slice();
        });
        this.form.controls['startAtZeroFromBeginning'].setValue(UserPreferenceService.get(UserPreference.START_AT_ZERO_FROM_BEGINNING, true) || this.form.value.nonGrouped);
        this.form.controls['startAtZeroAfterDateAggregation'].setValue(UserPreferenceService.get(UserPreference.START_AT_ZERO_AFTER_DATE_RANGE, true));
        this.form.controls['showOther'].setValue(UserPreferenceService.get(UserPreference.MONEY_SHOW_OTHER, true));
        this.form.controls['accountIds'].setValue(UserPreferenceService.get(UserPreference.ACCOUNT_IDS, '').toString().split(',').map(v => parseInt(v)));
        this.form.controls['currencies'].setValue(UserPreferenceService.get(UserPreference.CURRENCY_FILTER, '').toString().split(',').map(v => parseInt(v)));
        this.form.controls['groupIds'].setValue(UserPreferenceService.get(UserPreference.GROUP_IDS, '').toString().split(',').map(v => parseInt(v)));
        this.form.controls['dateRange'].setValue(UserPreferenceService.get(UserPreference.DATE_RANGE, TransactionDateRange.MONTH));
        this.form.controls['aggregateDateRange'].setValue(UserPreferenceService.get(UserPreference.AGGREGATE_DATE_RANGE, true));
        this.form.controls['amountOfOtherGroups'].setValue(UserPreferenceService.get(UserPreference.AMOUNT_OF_OTHER_GROUPS, 5));
        this.form.controls['startDate'].setValue(UserPreferenceService.get(UserPreference.START_DATE, null));
        this.form.controls['endDate'].setValue(UserPreferenceService.get(UserPreference.END_DATE, null));
        this.form.controls['includeBudget'].setValue(UserPreferenceService.get(UserPreference.START_AT_ZERO_AFTER_DATE_RANGE, true) && UserPreferenceService.get(UserPreference.MONEY_GRAPH_INCLUDE_BUDGET, true));
        this.form.controls['includeMoney'].setValue(UserPreferenceService.get(UserPreference.MONEY_GRAPH_INCLUDE_MONEY, true));
        this.form.controls['includeMoneyHolding'].setValue(UserPreferenceService.get(UserPreference.MONEY_GRAPH_INCLUDE_MONEY_HOLDING, true));
        this.form.controls['transactionFilter'].setValue(UserPreferenceService.get(UserPreference.TRANSACTION_FILTER, ''));

        if (this.form.value.nonGrouped) {
          this.form.get('startAtZeroFromBeginning')?.disable();
        } else {
          this.form.get('startAtZeroFromBeginning')?.enable();
        }
        this.onFilterUpdateCallback.next(this.getTransactionFilter());
      });
    });
  }

  getTransactionFilter() {
    let filter = new MoneyTransactionsFilter();
    filter.accountIds = this.form.value.accountIds;
    filter.currencies = this.form.value.currencies;
    filter.groupIds = this.form.value.groupIds == null ? [] : this.form.value.groupIds.filter((id: any) => id != null && !Number.isNaN(id));
    filter.aggregateDateRange = this.form.value.aggregateDateRange;
    filter.startAtZeroFromBeginning = this.form.value.startAtZeroFromBeginning;// || this.form.value.aggregateDateRange;
    filter.startAtZeroAfterDateAggregation = this.form.value.startAtZeroAfterDateAggregation;
    filter.showOther = this.form.value.showOther;
    filter.nonGrouped = this.form.value.nonGrouped;
    filter.dateRange = this.form.value.dateRange;
    filter.startDate = this.form.value.startDate == null ? null : moment(this.form.value.startDate).format("yyyy-MM-DD");
    filter.endDate = this.form.value.endDate == null ? null : moment(this.form.value.endDate).format("yyyy-MM-DD");
    filter.amountOfOtherGroups = this.form.value.amountOfOtherGroups;
    filter.includeBudget = this.form.value.includeBudget;
    filter.includeMoney = this.form.value.includeMoney;
    filter.includeMoneyHolding = this.form.value.includeMoneyHolding;
    filter.transactionFilter = this.form.value.transactionFilter;
    return filter;
  }

  onUpdateFilterButtonClicked() {
    this.onFilterUpdateCallback.next(this.getTransactionFilter());
  }

  onAggregateDateRangeChanged(checked: boolean) {
    if (this.form.value.aggregateDateRange) {
      if (this.form.value.nonGrouped) {
        this.form.controls['startAtZeroFromBeginning'].setValue(true);
        UserPreferenceService.set(UserPreference.START_AT_ZERO_FROM_BEGINNING, true);
      }
    } else {
      this.UserPreferenceService.set(UserPreference.DATE_RANGE, null);
      this.form.controls['dateRange'].setValue(null);
    }
    UserPreferenceService.onCheckboxChange(UserPreference.AGGREGATE_DATE_RANGE, checked);
  }

  onGroupIdsSelectChanged(value: any[]) {
    UserPreferenceService.onSelectChange(UserPreference.GROUP_IDS, value);
  }

  onAccountIdsSelectChanged(value: any[]) {
    UserPreferenceService.onSelectChange(UserPreference.ACCOUNT_IDS, value);
  }

  onCurrencyFilterChanged(value: any[]) {
    UserPreferenceService.onSelectChange(UserPreference.CURRENCY_FILTER, value);
  }

  selected(event: MatAutocompleteSelectedEvent): void {
    this.selectedGroups.update(fruits => [...fruits, event.option.viewValue]);
    this.currentGroup.set('');
    event.option.deselect();
  }

  onIncludeMoneyChanged($event: any) {
    UserPreferenceService.onCheckboxChange(UserPreference.MONEY_GRAPH_INCLUDE_MONEY, $event.checked);

    if (!$event.checked) {
      this.form.controls['includeBudget'].setValue(false);
      UserPreferenceService.onCheckboxChange(UserPreference.MONEY_GRAPH_INCLUDE_BUDGET, false);

    }
  }

  onStartAtZeroAfterDateAggregationChanged($event: MatCheckboxChange) {
    UserPreferenceService.onCheckboxChange(UserPreference.START_AT_ZERO_AFTER_DATE_RANGE, $event.checked);
    this.includeBudgetDisabled = !$event.checked;
    if ($event.checked) {
      this.form.controls['startAtZeroFromBeginning'].setValue(true);
    } else {
      this.form.controls['includeBudget'].setValue(false);
    }
  }

  onRouteToMoneySettings() {
    this.router.navigate(['/money/transaction-group'])
  }

  protected readonly Util = Util;
  protected readonly CacheService = CacheService;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
}
