import {Component, EventEmitter, Input, OnInit, ViewChild} from '@angular/core';
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
import {MatButton} from "@angular/material/button";
import {TranslatePipe} from "@ngx-translate/core";
import {DatePipe, NgIf} from "@angular/common";
import {MoneyApi} from "../../api/money-api";
import MoneyTransaction from "../../dto/money/money-transaction";
import {Util} from "../../util";
import {MatFormField, MatInput, MatLabel} from "@angular/material/input";
import {MatSort} from "@angular/material/sort";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {CurrencyComponent} from "../../components/currency/currency.component";
import {MoneyTransactionsFilter} from "../../dto/money/money-transactions-filter";
import moment from "moment/moment";
import {AuthService} from "../../service/auth.service";
import {MatIcon} from "@angular/material/icon";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {DialogService} from "../../service/dialog.service";
import {TranslateService} from "../../service/translate.service";

@Component({
  selector: 'app-moneyTransaction-list',
  standalone: true,
  imports: [
    MatTable,
    MatColumnDef,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatHeaderRowDef,
    MatHeaderCellDef,
    MatCellDef,
    MatRowDef,
    TranslatePipe,
    NgIf,
    MatInput,
    MatLabel,
    MatFormField,
    DatePipe,
    MatButton,
    PaginatorComponent,
    CurrencyComponent,
    MatIcon,
    MatMenu,
    MatMenuItem,
    MatMenuTrigger
  ],
  providers: [TranslatePipe],
  templateUrl: './money-transaction-list.component.html',
  styleUrl: './money-transaction-list.component.scss'
})
export class MoneyTransactionListComponent implements OnInit {
  displayedColumns: string[] = ['timestamp', 'debit-credit', 'counterpart-account-name', 'counterpart-account-number', 'amount', 'group', 'description', 'options'];
  dataSource: MatTableDataSource<MoneyTransaction>;
  moneyTransactions: MoneyTransaction[];
  groupsById: any = {};

  @Input()
  transactionFilter: MoneyTransactionsFilter;

  @Input() onFilterUpdateCallback: EventEmitter<MoneyTransactionsFilter>;

  @Input() accountId: number;
  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  constructor(
    private authService: AuthService,
    private dialogService: DialogService,
    private translateService: TranslateService,
    private api: MoneyApi) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.onFilterUpdateCallback.subscribe(filter => {
        this.transactionFilter = filter;
        this.resetTransactions();
      });
    });
  }

  resetTransactions() {
    this.api.getMoneyTransactionGroups().subscribe(groups => {
      groups.forEach(group => this.groupsById[group.id] = group);
      this.api.getTransactions(this.transactionFilter).subscribe(moneyTransactions => {
        let startDate = this.getStartDate();
        moneyTransactions.forEach(transaction => transaction.groupName = this.getGroupName(transaction.groupId));
        this.moneyTransactions = moneyTransactions
          .filter(transaction => startDate == null || startDate.isSameOrBefore(moment(transaction.timestamp)));
        this.dataSource = new MatTableDataSource<MoneyTransaction>(moneyTransactions.reverse());

        setTimeout(() => {
          this.dataSource.paginator = this.paginator.getPaginator();
          this.dataSource.sort = this.sort;
        });
      });
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  getStartDate() {
    return this.transactionFilter.startDate == null ? null : moment(this.transactionFilter.startDate);
  }

  getGroupName(groupId: number) {
    if (this.groupsById[groupId] == null) {
      return '';
    }

    return this.groupsById[groupId].name;
  }

  onSetTransactionGroup(transaction: MoneyTransaction) {
    this.dialogService.setTransactionGroup(transaction.groupId).then(group => {
      if (group != null) {
        let groupId = group.id == -1 ? null : group.id;
        this.api.setTransactionGroup(transaction.id, groupId).subscribe(() => this.resetTransactions());
      }
    })
  }

  onDeleteTransaction(transaction: MoneyTransaction) {
    this.dialogService.deleteConfirmation().subscribe(() => {
      this.api.deleteMoneyTransaction(transaction.id).subscribe(() => this.dialogService.snackbar(
        this.translateService.translate('security/holding/deleted-message'),
        this.translateService.translate('close')));
      // todo, refresh table?
    });
  }

  protected readonly Util = Util;
}
