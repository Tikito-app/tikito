import {AfterViewInit, Component, EventEmitter, Output, ViewChild} from '@angular/core';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {BudgetApi} from "../../api/budget-api";
import Budget from "../../dto/budget/budget";
import {NgIf} from "@angular/common";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {BudgetDateRange} from "../../dto/budget/budget-date-range";
import {Util} from "../../util";
import moment from "moment";
import {HistoricalBudgetValue} from "../../dto/budget/historical-budget-value";
import {TranslatePipe} from "@ngx-translate/core";
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {MatButton, MatFabButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {MoneyTransactionsFilter} from "../../dto/money/money-transactions-filter";
import {MatOption} from "@angular/material/core";
import {MatFormField, MatLabel, MatSelect} from "@angular/material/select";
import {Observable} from "rxjs";
import {Month} from "../../dto/month";

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgIf,
    TranslatePipe,
    MatColumnDef,
    MatTable,
    MatTab,
    MatTabGroup,
    MatHeaderCell,
    MatFabButton,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatPaginator,
    MatCellDef,
    MatHeaderCellDef,
    MatHeaderRowDef,
    MatRowDef,
    MatIcon,
    MatButton,
    MatOption,
    MatSelect,
    MatFormField,
    MatLabel
  ],
  templateUrl: './budget-list.component.html',
  styleUrl: './budget-list.component.scss'
})
export class BudgetListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'amount', 'spent'];
  dataSource: MatTableDataSource<Budget>;
  budgets: Budget[];
  selectedBudget: Budget;
  budgetForm: FormGroup = new FormGroup({
    year: new FormControl(),
    month: new FormControl()
  });
  startDate: FormControl = new FormControl();
  endDate: FormControl = new FormControl();
  historicalValuesPerBudgetId: any = {};
  currentValuePerBudgetIdAndDateRange: any = {};
  currentDateRangeStrings: any = {};
  budgetsPerId: any = {};
  yearsInBudget: number[] = [];

  @ViewChild(MatPaginator) paginator: MatPaginator;

  @Output()
  onTransactionFilterCallback: EventEmitter<MoneyTransactionsFilter> = new EventEmitter();

  constructor(
    private router: Router,
    private api: BudgetApi) {
    this.currentDateRangeStrings[BudgetDateRange.YEAR] = moment().format(Util.getDateRangeFormat(BudgetDateRange.YEAR));
    this.currentDateRangeStrings[BudgetDateRange.MONTH] = moment().format(Util.getDateRangeFormat(BudgetDateRange.MONTH));
    this.currentDateRangeStrings[BudgetDateRange.WEEK] = moment().format(Util.getDateRangeFormat(BudgetDateRange.WEEK));
    this.currentDateRangeStrings[BudgetDateRange.DAY] = moment().format(Util.getDateRangeFormat(BudgetDateRange.DAY));
    this.currentDateRangeStrings[BudgetDateRange.ONCE] = moment().format(Util.getDateRangeFormat(BudgetDateRange.ONCE));
  }

  ngAfterViewInit() {
    // this.api.getBudgets().subscribe(budgets => {
    //   this.budgets = budgets;
    //   this.budgets.forEach(budget => {
    //     this.historicalValuesPerBudgetId[budget.id] = [];
    //     this.budgetsPerId[budget.id] = budget;
    //     this.budgetForm.addControl('budget-' + budget.id, new FormControl())
    //   });
    //
    //   this.api.getHistoricalValues().subscribe(values => {
    //     values.forEach(value => {
    //       if (this.historicalValuesPerBudgetId[value.budgetId] == null) {
    //         this.historicalValuesPerBudgetId[value.budgetId] = {};
    //         this.currentValuePerBudgetIdAndDateRange[value.budgetId] = [];
    //       }
    //       let date = moment(value.date);
    //       this.historicalValuesPerBudgetId[value.budgetId][date.format(Util.getDateRangeFormat(this.budgetsPerId[value.budgetId].dateRange))] = value;
    //       if(!this.yearsInBudget.includes(date.year())) {
    //         this.yearsInBudget.push(date.year());
    //       }
    //     });
    //   });

    //   this.dataSource = new MatTableDataSource<Budget>(budgets);
    //   this.dataSource.paginator = this.paginator;
    // });
  }

  onBudgetSelectedChanged() {
    let selectedBudgetIds: number[] = [];
    // this.budgets.forEach(budget => {
    //   if (this.budgetForm.controls['budget-' + budget.id].value) {
    //     selectedBudgetIds.push(budget.id);
    //   }
    // });
    // console.log(selectedBudgetIds);
    // if (selectedBudgetIds.length == 1) {
      this.selectedBudget = this.budgetsPerId[selectedBudgetIds[0]];
    let month = this.monthToNumberedString(this.budgetForm.value['month'])
    let year = this.budgetForm.value['year'];
    console.log(month.toString());
      setTimeout(() => {

        // console.log(this.getTransactionFilter())
        let filter = this.getTransactionFilter();
        this.api.getBudgetsByFilter(filter).subscribe(budgets => {

          this.fetchHistoricalValues().subscribe(() => {
            this.budgets = budgets;
            console.log(this.budgets);
            console.log(this.historicalValuesPerBudgetId);
            this.budgets.forEach(budget => {
              if(this.historicalValuesPerBudgetId[budget.id] != null && this.historicalValuesPerBudgetId[budget.id][year + '-' + month] != null) {
                let historicalValue = this.historicalValuesPerBudgetId[budget.id][year + '-' + month];
                budget.spent = historicalValue.spent;
                console.log(budget.spent);
              }
            });
            this.dataSource = new MatTableDataSource<Budget>(budgets);
            this.dataSource.paginator = this.paginator;
          });
        });
        this.onTransactionFilterCallback.next(filter);
      }, 500);
    // } else if (selectedBudgetIds.length > 1) {
    //
    // }
  }

  monthToNumberedString(month: Month): string {
    if(month == Month.JANUARY) {
      return '1';
    } else if(month == Month.FEBRUARY) {
      return '2';
    } else if(month == Month.MARCH) {
      return '3';
    } else if(month == Month.APRIL) {
      return '4';
    } else if(month == Month.MAY) {
      return '5';
    } else if(month == Month.JUNE) {
      return '6';
    } else if(month == Month.JULY) {
      return '7';
    } else if(month == Month.AUGUST) {
      return '8';
    } else if(month == Month.SEPTEMBER) {
      return '9';
    } else if(month == Month.OCTOBER) {
      return '10';
    } else if(month == Month.NOVEMBER) {
      return '11';
    }
    return '12';
  }

  getValueForDate(budgetId: number): HistoricalBudgetValue {
    let dateRangeString = moment().format(Util.getDateRangeFormat(this.budgetsPerId[budgetId].dateRange));
    console.log(dateRangeString);
    return this.historicalValuesPerBudgetId[budgetId][dateRangeString];
  }

  onAddBudgetButtonClicked() {
    this.router.navigate(['/budget/create']);
  }

  mapGroups(groups: MoneyTransactionGroup[]): string {
    return groups.map((group: MoneyTransactionGroup) => group.name).join(',');
  }

  onRowClicked(row: Budget) {
    this.router.navigate(['/budget/' + row.id]);
  }

  getTransactionFilter(): MoneyTransactionsFilter {
    let filter = new MoneyTransactionsFilter();
    // filter.accountIds = [];
    let month = this.budgetForm.value['month'];

    let startDate = this.getStartDate();
    filter.startDate = startDate.toISOString();
    filter.endDate = moment(startDate).add(1, month == null ? 'year' : 'month').toISOString();
    // filter.groupIds = this.selectedBudget.groups.map(group => group.id);
    // this.selectedBudget.groups.forEach(group => filter.accountIds?.push(...group.accountIds));
    return filter;
  }

  getStartDate(): moment.Moment {
    let month = this.budgetForm.value['month'];
    let year = this.budgetForm.value['year'];
    // if(month == null) {
    //   return moment('1-1-' + year);
    // }
    return moment('1-' + month + '-' + year);
  }

  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
  protected readonly Util = Util;

  private fetchHistoricalValues(): Observable<void> {
    let month = this.budgetForm.value['month'];
    return new Observable(subscriber => {
      this.api.getHistoricalValues().subscribe(values => {
        values.forEach(value => {
          if (this.historicalValuesPerBudgetId[value.budgetId] == null) {
            this.historicalValuesPerBudgetId[value.budgetId] = {};
            // this.currentValuePerBudgetIdAndDateRange[value.budgetId] = [];
          }

          let date = moment(value.date);
          // if(month == null) {
          //   this.historicalValuesPerBudgetId[value.budgetId][date.year() + '-1'] = value;
          // } else {
            this.historicalValuesPerBudgetId[value.budgetId][date.year() + '-' + date.month()] = value;
          // }
          // this.historicalValuesPerBudgetId[value.budgetId][date.format(Util.getDateRangeFormat(this.budgetsPerId[value.budgetId].dateRange))] = value;
          // if (!this.yearsInBudget.includes(date.year())) {
          //   this.yearsInBudget.push(date.year());
          // }
        });
        subscriber.next();
      });
    });
  }
}
