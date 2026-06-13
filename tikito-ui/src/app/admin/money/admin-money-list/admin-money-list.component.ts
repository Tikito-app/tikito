import {AfterViewInit, Component, ViewChild} from '@angular/core';
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
import {Security} from "../../../dto/security/security";
import {PaginatorComponent} from "../../../components/paginator/paginator.component";
import {Router} from "@angular/router";
import {AuthService} from "../../../service/auth.service";
import {DialogService} from "../../../service/dialog.service";
import {AdminApi} from "../../../api/admin-api";
import {CacheService} from "../../../service/cache-service";
import {MatButton} from "@angular/material/button";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {Account} from "../../../dto/account";
import {TranslatePipe, TranslateService} from "@ngx-translate/core";
import {MatCard, MatCardHeader} from "@angular/material/card";
import {AccountApi} from "../../../api/account-api";

@Component({
  selector: 'app-admin-money-list',
  standalone: true,
  imports: [
    MatButton,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatFormField,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatIcon,
    MatInput,
    MatLabel,
    MatMenu,
    MatMenuItem,
    MatRow,
    MatRowDef,
    MatTable,
    PaginatorComponent,
    MatMenuTrigger,
    MatHeaderCellDef,
    TranslatePipe,
    MatCard,
    MatCardHeader
  ],
  templateUrl: './admin-money-list.component.html',
  styleUrl: './admin-money-list.component.scss'
})
export class AdminMoneyListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'currency', 'options'];
  dataSource: MatTableDataSource<Account>;
  moneyAccounts: Account[];

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;

  constructor(
    private router: Router,
    private authService: AuthService,
    private dialogService: DialogService,
    private accountApi: AccountApi,
    private translateService: TranslateService,
    private api: AdminApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.accountApi.getAccounts().subscribe(moneyAccounts => {
        this.moneyAccounts = moneyAccounts;

        this.dataSource = new MatTableDataSource<Account>(moneyAccounts);
        setTimeout(() => {
          this.dataSource.paginator = this.paginator.getPaginator();
        });
      });
    });
  }

  getCurrencyName(security: Security): string {
    let currency = CacheService.getCurrencyById(security.currencyId);
    if (currency == null) {
      return '';
    }
    return currency.name;
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }

  onRecalculateHistoricalMoneyValue(account: Account) {
    this.api.recalculateHistoricalMoneyValue(account.id).subscribe(() => {
      this.dialogService.snackbar(this.translateService.instant('done'), this.translateService.instant('done'));
    });
  }

  onRecalculateHistoricalBudgetValue() {
    this.api.recalculateHistoricalBudgetValue().subscribe(() => {
      this.dialogService.snackbar(this.translateService.instant('done'), this.translateService.instant('done'));
    });
  }

  onGroupMoneyTransactions() {
    this.api.groupMoneyTransactions().subscribe(() => {
      this.dialogService.snackbar(this.translateService.instant('done'), this.translateService.instant('done'));
    });
  }
}
