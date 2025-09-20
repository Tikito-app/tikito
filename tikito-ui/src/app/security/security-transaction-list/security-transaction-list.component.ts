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
import {Router} from "@angular/router";
import {TranslatePipe} from "@ngx-translate/core";
import {DatePipe, NgIf} from "@angular/common";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {SecurityApi} from "../../api/security-api";
import {SecurityTransaction} from "../../dto/security/security-transaction";
import {Util} from "../../util";
import {SecurityHoldingFilter} from "../../dto/security/security-holding-filter";
import {CurrencyComponent} from "../../components/currency/currency.component";
import {AuthService} from "../../service/auth.service";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import MoneyTransaction from "../../dto/money/money-transaction";
import {DialogService} from "../../service/dialog.service";

@Component({
  selector: 'app-security-transaction-list',
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
    PaginatorComponent,
    DatePipe,
    CurrencyComponent,
    MatButton,
    MatIcon,
    MatMenu,
    MatMenuItem,
    MatMenuTrigger
  ],
  providers: [TranslatePipe],
  templateUrl: './security-transaction-list.component.html',
  styleUrl: './security-transaction-list.component.scss'
})
export class SecurityTransactionListComponent implements OnInit {
  displayedColumns: string[] = ['date', 'trading-company', 'transaction-type', 'amount', 'price', 'currency', 'options'];
  dataSource: MatTableDataSource<SecurityTransaction>;
  transactions: SecurityTransaction[];

  @ViewChild(PaginatorComponent)
  paginator: PaginatorComponent;

  @Input()
  onFilterUpdateCallback: EventEmitter<SecurityHoldingFilter>;

  @Input()
  securityHoldingFilter: SecurityHoldingFilter;


  constructor(
    private router: Router,
    private dialogService: DialogService,
    private translate: TranslatePipe,
    private authService: AuthService,
    private api: SecurityApi) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.onFilterUpdateCallback.subscribe(filter => {
        this.securityHoldingFilter = filter;
        this.resetTable();
      });
    });
  }

  resetTable() {
    this.api.getTransactions(this.securityHoldingFilter).subscribe(transactions => {
      this.transactions = transactions;

      this.dataSource = new MatTableDataSource<SecurityTransaction>(transactions);
      setTimeout(() => {
        this.dataSource.paginator = this.paginator.getPaginator();
      });
    });
  }

  onDeleteTransaction(transaction: MoneyTransaction) {
    this.dialogService.okCancel(
      this.translate.transform('are-you-sure-delete-title'),
      this.translate.transform('are-you-sure-delete-text'))
      .then((doDelete) => {
        if (doDelete) {
          this.api.deleteSecurityTransaction(transaction.id).subscribe(() => this.dialogService.snackbar(
            this.translate.transform('security/holding/deleted-message'),
            this.translate.transform('close')));
        }
      })
  }

  protected readonly Util = Util;
}
