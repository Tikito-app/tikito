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
import {TranslatePipe} from "@ngx-translate/core";
import {DialogService} from "../../../service/dialog.service";
import {AdminApi} from "../../../api/admin-api";
import {CacheService} from "../../../service/cache-service";
import {MatButton} from "@angular/material/button";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {NgIf} from "@angular/common";
import {Account} from "../../../dto/account";
import {TranslateService} from "../../../service/translate.service";

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
    NgIf,
    PaginatorComponent,
    TranslatePipe,
    MatMenuTrigger,
    MatHeaderCellDef
  ],
  providers: [TranslatePipe],
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
    private translateService: TranslateService,
    private dialogService: DialogService,
    private api: AdminApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.api.getMoneyAccounts().subscribe(moneyAccounts => {
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
      this.dialogService.snackbar(this.translateService.translate('done'), this.translateService.translate('done'));
    });
  }

  onGroupMoneyTransactions() {
    this.api.groupMoneyTransactions().subscribe(() => {
      this.dialogService.snackbar(this.translateService.translate('done'), this.translateService.translate('done'));
    });
  }
}
