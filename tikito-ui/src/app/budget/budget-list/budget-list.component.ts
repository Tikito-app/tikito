import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {Router} from "@angular/router";
import {BudgetApi} from "../../api/budget-api";
import Budget from "../../dto/budget/budget";
import {NgForOf, NgIf} from "@angular/common";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCheckbox} from "@angular/material/checkbox";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {BudgetDateRange} from "../../dto/budget/budget-date-range";
import {Util} from "../../util";
import moment from "moment";
import {HistoricalBudgetValue} from "../../dto/budget/historical-budget-value";

@Component({
    selector: 'app-budget-list',
    standalone: true,
    imports: [
        NgForOf,
        FormsModule,
        MatCheckbox,
        ReactiveFormsModule,
        NgIf
    ],
    templateUrl: './budget-list.component.html',
    styleUrl: './budget-list.component.scss'
})
export class BudgetListComponent implements AfterViewInit {
    displayedColumns: string[] = ['name', 'amount', 'date-range', 'groups'];
    dataSource: MatTableDataSource<Budget>;
    budgets: Budget[];
    budgetForm: FormGroup = new FormGroup({});
    historicalValuesPerBudgetId: any = {};
    currentValuePerBudgetIdAndDateRange: any = {};
    currentDateRangeStrings: any = {};
    budgetsPerId: any = {};

    @ViewChild(MatPaginator) paginator: MatPaginator;


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
        this.api.getBudgets().subscribe(budgets => {
            this.budgets = budgets;
            this.budgets.forEach(budget => {
                this.historicalValuesPerBudgetId[budget.id] = [];
                this.budgetsPerId[budget.id] = budget;
                this.budgetForm.addControl('budget-' + budget.id, new FormControl())
            });

            this.api.getHistoricalValues().subscribe(values => {
                values.forEach(value => {
                    if(this.historicalValuesPerBudgetId[value.budgetId] == null) {
                        this.historicalValuesPerBudgetId[value.budgetId] = {};
                        this.currentValuePerBudgetIdAndDateRange[value.budgetId] = [];
                    }
                    this.historicalValuesPerBudgetId[value.budgetId][moment(value.date).format(Util.getDateRangeFormat(this.budgetsPerId[value.budgetId].dateRange))] = value;
                });
            });

            this.dataSource = new MatTableDataSource<Budget>(budgets);
            this.dataSource.paginator = this.paginator;
        });
    }

    onBudgetSelectedChanged() {
        let selectedBudgetIds: number[] = [];
        this.budgets.forEach(budget => {
            if(this.budgetForm.controls['budget-' + budget.id].value) {
                selectedBudgetIds.push(budget.id);
            }
        });
        console.log(selectedBudgetIds);
        if(selectedBudgetIds.length == 1) {

        } else if(selectedBudgetIds.length > 1) {

        }
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

    protected readonly UserPreferenceService = UserPreferenceService;
    protected readonly UserPreference = UserPreference;
}
