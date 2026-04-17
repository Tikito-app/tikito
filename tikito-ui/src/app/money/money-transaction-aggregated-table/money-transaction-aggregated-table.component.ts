import {Component, EventEmitter, Input, OnInit} from '@angular/core';
import {MoneyTransactionsFilter, TransactionDateRange} from "../../dto/money/money-transactions-filter";
import {Observable} from "rxjs";
import {MoneyApi} from "../../api/money-api";
import {AuthService} from "../../service/auth.service";
import {BudgetApi} from "../../api/budget-api";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import MoneyTransaction from "../../dto/money/money-transaction";
import {HistoricalBudgetValue} from "../../dto/budget/historical-budget-value";
import {MoneyBudgetTransaction} from "../../dto/money/money-budget-transaction";
import moment from "moment";
import {Util} from "../../util";
import {MatTableModule} from "@angular/material/table";
import {TranslatePipe} from "../../service/translate-pipe.pipe";
import {CommonModule} from "@angular/common";
import {MatTabChangeEvent, MatTabGroup, MatTab} from "@angular/material/tabs";

class AggregatedGroupData {
  name: string;
  spent: number = 0;
  budgeted: number = 0;

  constructor(name: string) {
    this.name = name;
  }
}

class TabInfo {
  label: string;
  start: moment.Moment;
  end: moment.Moment;

  constructor(label: string, start: moment.Moment, end: moment.Moment) {
    this.label = label;
    this.start = start;
    this.end = end;
  }
}

@Component({
  selector: 'app-money-transaction-aggregated-table',
  standalone: true,
  imports: [MatTableModule, TranslatePipe, CommonModule, MatTabGroup, MatTab],
  templateUrl: './money-transaction-aggregated-table.component.html',
  styleUrl: './money-transaction-aggregated-table.component.scss'
})
export class MoneyTransactionAggregatedTableComponent implements OnInit {

  @Input()
  transactionFilter: MoneyTransactionsFilter;

  @Input()
  onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter>;

  moneyTransactions: MoneyTransaction[];
  historicalBudgetValues: HistoricalBudgetValue[];
  moneyTransactionGroups: MoneyTransactionGroup[] = [];
  groupsById: any;
  budgetsById: any;
  budgets: any[];

  aggregatedData: AggregatedGroupData[] = [];
  displayedColumns: string[] = ['name', 'spent', 'budgeted'];

  tabs: TabInfo[] = [];
  selectedTabIndex: number = 0;

  protected readonly Util = Util;

  constructor(private api: MoneyApi,
              private authService: AuthService,
              private budgetApi: BudgetApi) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.onFilterUpdateCallback.subscribe(filter => {
        this.transactionFilter = filter;
        this.refreshData();
      });
      this.api.getMoneyTransactionGroups().subscribe(groups => {
        this.moneyTransactionGroups = groups;
        this.groupsById = {};
        this.moneyTransactionGroups.forEach(group => {
          this.groupsById[group.id] = group;
        });
      });
    });
  }

  refreshData() {
    this.assertHasTransactions().subscribe(() => {
      this.assertHasBudget().subscribe(() => {
        this.generateTabs();
        this.aggregateData();
      });
    });
  }

  assertHasTransactions(): Observable<void> {
    return new Observable(observer => {
      if (!this.transactionFilter.includeMoney) {
        this.moneyTransactions = [];
        observer.next();
      } else {
        this.api.getTransactions(this.transactionFilter.withoutStartAndEndDate()).subscribe(transactions => {
          this.moneyTransactions = transactions;
          observer.next();
        });
      }
    });
  }

  assertHasBudget(): Observable<void> {
    this.historicalBudgetValues = [];
    return new Observable(observer => {
      if (!this.transactionFilter.includeBudget) {
        observer.next();
        return;
      }

      this.budgetApi.getBudgets().subscribe(budgets => {
        this.budgets = budgets;
        this.budgetsById = {};
        this.budgets.forEach(budget => {
          this.budgetsById[budget.id] = budget;
        });

        // this.budgetApi.getHistoricalValues().subscribe(historicalBudgetValues => {
        //   this.historicalBudgetValues = historicalBudgetValues;
        //   observer.next();
        // })
      });
    });
  }

  generateTabs() {
    this.tabs = [];
    this.selectedTabIndex = 0;

    let startDate = this.transactionFilter.startDate ? moment(this.transactionFilter.startDate) : this.findEarliestDate();
    let endDate = this.transactionFilter.endDate ? moment(this.transactionFilter.endDate) : moment();

    if (!startDate) {
      // If no data and no filter, just show current period or "All"
      startDate = moment().startOf('year');
    }
    
    // Safety check if startDate is after endDate
    if (startDate.isAfter(endDate)) {
       startDate = endDate.clone().startOf('day');
    }

    let range = this.transactionFilter.dateRange;

    // If range is null or ALL, create a single tab.
    if (!range || range === TransactionDateRange.ALL) {
      this.tabs.push(new TabInfo('All', startDate, endDate));
      return;
    }

    let current = startDate.clone();
    
    // Align start date to the beginning of the period
    if (range === TransactionDateRange.YEAR) current.startOf('year');
    if (range === TransactionDateRange.MONTH) current.startOf('month');
    if (range === TransactionDateRange.WEEK) current.startOf('isoWeek');

    while (current.isSameOrBefore(endDate)) {
       let tabStart = current.clone();
       let tabEnd = current.clone(); // Placeholder
       let label = '';

       if (range === TransactionDateRange.YEAR) {
          label = current.format('YYYY');
          tabEnd.endOf('year');
          current.add(1, 'year');
       } else if (range === TransactionDateRange.MONTH) {
          label = current.format('YYYY-MMM');
          tabEnd.endOf('month');
          current.add(1, 'month');
       } else if (range === TransactionDateRange.WEEK) {
          label = current.format('YYYY-WW');
          tabEnd.endOf('isoWeek');
          current.add(1, 'week');
       } else {
           // Fallback for day or others
           label = current.format('YYYY-MM-DD');
           tabEnd.endOf('day');
           current.add(1, 'day');
       }
       
       this.tabs.push(new TabInfo(label, tabStart, tabEnd));
    }
  }

  findEarliestDate(): moment.Moment | null {
    let minDate: moment.Moment | null = null;
    
    if (this.moneyTransactions) {
      this.moneyTransactions.forEach(t => {
        const d = moment(t.timestamp);
        if (!minDate || d.isBefore(minDate)) minDate = d;
      });
    }
    if (this.historicalBudgetValues) {
      this.historicalBudgetValues.forEach(t => {
        const d = moment(t.date);
        if (!minDate || d.isBefore(minDate)) minDate = d;
      });
    }
    return minDate;
  }

  onTabChanged(event: MatTabChangeEvent) {
    this.selectedTabIndex = event.index;
    this.aggregateData();
  }

  aggregateData() {
    let dataMap: { [key: string]: AggregatedGroupData } = {};

    let startDate: moment.Moment | null = null;
    let endDate: moment.Moment | null = null;

    if (this.tabs.length > 0 && this.selectedTabIndex < this.tabs.length) {
      startDate = this.tabs[this.selectedTabIndex].start;
      endDate = this.tabs[this.selectedTabIndex].end;
    }

    // Process Money Transactions
    if (this.moneyTransactions) {
      this.moneyTransactions.forEach(transaction => {
        let date = moment(transaction.timestamp);
        if (startDate && date.isBefore(startDate)) return;
        if (endDate && date.isAfter(endDate)) return;

        let groupName = this.getGroupName(transaction);
        if (!dataMap[groupName]) {
          dataMap[groupName] = new AggregatedGroupData(groupName);
        }
        dataMap[groupName].spent += this.getPositiveValue(transaction.amount);
      });
    }

    // Process Budget Values
    if (this.historicalBudgetValues) {
      this.historicalBudgetValues.forEach(budgetValue => {
        let date = moment(budgetValue.date);
        if (startDate && date.isBefore(startDate)) return;
        if (endDate && date.isAfter(endDate)) return;

        let groupName = this.budgetsById[budgetValue.budgetId].name;
        if (!dataMap[groupName]) {
          dataMap[groupName] = new AggregatedGroupData(groupName);
        }
        dataMap[groupName].budgeted += budgetValue.budgeted;
      });
    }

    this.aggregatedData = Object.values(dataMap).sort((a, b) => b.spent - a.spent);
  }

  getGroupName(transaction: MoneyTransaction): string {
    if (transaction.groupId != null && this.groupsById[transaction.groupId]) {
      return this.groupsById[transaction.groupId].name;
    }
    if (transaction.counterpartAccountName != null) {
      return transaction.counterpartAccountName;
    }
    if (transaction.counterpartAccountNumber != null) {
      return transaction.counterpartAccountNumber;
    }
    return transaction.description;
  }

  getPositiveValue(value: number): number {
    if (value == null || isNaN(value)) {
      return 0;
    }
    return value < 0 ? -value : value;
  }
}
