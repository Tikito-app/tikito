import {Component, OnInit, ViewChild} from '@angular/core';
import {CurrencyComponent} from "../../components/currency/currency.component";
import {DatePipe, NgIf} from "@angular/common";
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
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {TranslatePipe} from "@ngx-translate/core";
import MoneyTransaction from "../../dto/money/money-transaction";
import {MatSort} from "@angular/material/sort";
import {AuthService} from "../../service/auth.service";
import {DialogService} from "../../service/dialog.service";
import {TranslateService} from "../../service/translate.service";
import {MoneyApi} from "../../api/money-api";
import {Util} from "../../util";

@Component({
  selector: 'app-loan-money-transactions-list',
  standalone: true,
  imports: [
    CurrencyComponent,
    DatePipe,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatFormField,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatInput,
    MatLabel,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf,
    PaginatorComponent,
    TranslatePipe,
    MatHeaderCellDef
  ],
  templateUrl: './loan-money-transactions-list.component.html',
  styleUrl: './loan-money-transactions-list.component.scss'
})
export class LoanMoneyTransactionsListComponent implements OnInit {
  displayedColumns: string[] = ['timestamp', 'counterpart-account-name', 'counterpart-account-number', 'amount', 'description'];
  dataSource: MatTableDataSource<MoneyTransaction>;
  moneyTransactions: MoneyTransaction[];

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
      this.resetTransactions();
    });
  }

  resetTransactions() {
    this.api.getTransactionsForLoans().subscribe(moneyTransactions => {
      this.moneyTransactions = moneyTransactions
      this.dataSource = new MatTableDataSource<MoneyTransaction>(moneyTransactions.reverse());

      setTimeout(() => {
        this.dataSource.paginator = this.paginator.getPaginator();
        this.dataSource.sort = this.sort;
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

  protected readonly Util = Util;
}
