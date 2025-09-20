import {Component, computed, EventEmitter, inject, model, OnInit, Output, signal} from '@angular/core';
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {TranslatePipe} from "@ngx-translate/core";
import {MoneyTransactionListComponent} from "../money-transaction-list/money-transaction-list.component";
import {MoneyTransactionGraphComponent} from "../money-transaction-graph/money-transaction-graph.component";
import {ActivatedRoute} from "@angular/router";
import {Util} from "../../util";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatCheckbox} from "@angular/material/checkbox";
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
import {provideNativeDateAdapter} from "@angular/material/core";
import {MatInput, MatInputModule} from "@angular/material/input";
import {MatRadioButton, MatRadioGroup} from "@angular/material/radio";
import {MoneyTransactionsFilter} from "../../dto/money/money-transactions-filter";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {MatIcon, MatIconModule} from "@angular/material/icon";
import {PopoverModule} from "../../components/popover/popover.module";
import {PopoverComponent} from "../../components/popover/popover.component";
import {NgIf} from "@angular/common";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {AccountType} from "../../dto/account-type";
import {MatChipInputEvent, MatChipsModule} from "@angular/material/chips";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {LiveAnnouncer} from "@angular/cdk/a11y";
import {COMMA, ENTER} from "@angular/cdk/keycodes";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-money-transaction-overview',
  standalone: true,
  imports: [
    MatTabGroup,
    MatTab,
    TranslatePipe,
    MoneyTransactionListComponent,
    MoneyTransactionGraphComponent,
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
    FormsModule
  ],
  templateUrl: './money-transaction-overview.component.html',
  styleUrl: './money-transaction-overview.component.scss',
  providers: [provideNativeDateAdapter()],
})
export class MoneyTransactionOverviewComponent implements OnInit {
  accountId: number;
  groups: MoneyTransactionGroup[];
  accounts: Account[];

  form: FormGroup;

  @Output()
  onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter> = new EventEmitter();

  constructor(private route: ActivatedRoute,
              private api: MoneyApi,
              private authService: AuthService,
              private accountApi: AccountApi) {
    this.accountId = Util.getIdFromRoute(route, 'accountId') as number;
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.form = new FormGroup({
        accountIds: new FormControl(),
        groupIds: new FormControl(),
        startAtZeroFromBeginning: new FormControl(),
        startAtZeroAfterDateAggregation: new FormControl(),
        showOther: new FormControl(),
        aggregateDateRange: new FormControl(),
        nonGrouped: new FormControl(),
        dateRange: new FormControl(),
        amountOfOtherGroups: new FormControl(),
        startDate: new FormControl,
        endDate: new FormControl
      });
      this.reset();
    });
  }

  reset(): void {
    let filter = new MoneyTransactionsFilter();
    filter.groupIds = this.form.value.groupIds == null ? [] : this.form.value.groupIds.filter((id: any) => id != null);
    filter.dateRange = this.form.value.dateRange;

    this.accountApi.getAccounts().subscribe(accounts => {
      this.accounts = accounts.filter(account => account.accountType == AccountType.DEBIT);
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
        this.form.controls['accountIds'].setValue(UserPreferenceService.get(UserPreference.ACCOUNT_IDS, '').toString().split(',').map(parseInt));
        this.form.controls['groupIds'].setValue(UserPreferenceService.get(UserPreference.GROUP_IDS, '').toString().split(',').map(parseInt));
        this.form.controls['dateRange'].setValue(UserPreferenceService.get(UserPreference.DATE_RANGE, null));
        this.form.controls['aggregateDateRange'].setValue(UserPreferenceService.get(UserPreference.AGGREGATE_DATE_RANGE, true));
        this.form.controls['amountOfOtherGroups'].setValue(UserPreferenceService.get(UserPreference.AMOUNT_OF_OTHER_GROUPS, 5));
        this.form.controls['startDate'].setValue(UserPreferenceService.get(UserPreference.START_DATE, null));
        this.form.controls['endDate'].setValue(UserPreferenceService.get(UserPreference.END_DATE, null));
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
    filter.groupIds = this.form.value.groupIds == null ? [] : this.form.value.groupIds.filter((id: any) => id != null);
    filter.aggregateDateRange = this.form.value.aggregateDateRange;
    filter.startAtZeroFromBeginning = this.form.value.startAtZeroFromBeginning;// || this.form.value.aggregateDateRange;
    filter.startAtZeroAfterDateAggregation = this.form.value.startAtZeroAfterDateAggregation;
    filter.showOther = this.form.value.showOther;
    filter.nonGrouped = this.form.value.nonGrouped;
    filter.dateRange = this.form.value.dateRange;
    filter.startDate = this.form.value.startDate;
    filter.endDate = this.form.value.endDate;
    filter.amountOfOtherGroups = this.form.value.amountOfOtherGroups;
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

  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;


  readonly separatorKeysCodes: number[] = [ENTER, COMMA];
  readonly currentGroup = model('');
  readonly selectedGroups = signal(['Lemon']);
  allGroups: string[] = [];
  filteredGroups: any;

  readonly announcer = inject(LiveAnnouncer);

  add(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    // Add our fruit
    if (value) {
      this.selectedGroups.update(fruits => [...fruits, value]);
    }

    // Clear the input value
    this.currentGroup.set('');
  }

  remove(fruit: string): void {
    this.selectedGroups.update(fruits => {
      const index = fruits.indexOf(fruit);
      if (index < 0) {
        return fruits;
      }

      fruits.splice(index, 1);
      this.announcer.announce(`Removed ${fruit}`);
      return [...fruits];
    });
  }

  selected(event: MatAutocompleteSelectedEvent): void {
    this.selectedGroups.update(fruits => [...fruits, event.option.viewValue]);
    this.currentGroup.set('');
    event.option.deselect();
  }
}
