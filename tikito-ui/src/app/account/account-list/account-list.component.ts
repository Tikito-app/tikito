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
import {MatIcon} from "@angular/material/icon";
import {MatButton, MatFabButton} from "@angular/material/button";
import {Router} from "@angular/router";
import {AccountApi} from "../../api/account-api";
import {TranslatePipe} from "@ngx-translate/core";
import {NgIf} from "@angular/common";
import {Account} from "../../dto/account";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {AuthService} from "../../service/auth.service";
import {CacheService} from "../../service/cache-service";
import {DialogService} from "../../service/dialog.service";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {TranslateService} from "../../service/translate.service";

@Component({
  selector: 'app-account-list',
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
    MatIcon,
    MatFabButton,
    TranslatePipe,
    NgIf,
    PaginatorComponent,
    MatButton,
    MatMenu,
    MatMenuItem,
    MatMenuTrigger
  ],
  providers: [TranslatePipe],
  templateUrl: './account-list.component.html',
  styleUrl: './account-list.component.scss'
})
export class AccountListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'account-type', 'account-number', 'currency', 'options'];
  dataSource: MatTableDataSource<Account>;
  accounts: Account[] = [];

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;

  constructor(
    private router: Router,
    private authService: AuthService,
    private translateService: TranslateService,
    private dialogService: DialogService,
    private api: AccountApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.reset();
    });
  }

  reset() {
    this.api.getAccounts().subscribe(accounts => {
      this.accounts = accounts;

      this.dataSource = new MatTableDataSource<Account>(accounts);
      setTimeout(() => {
        if (this.paginator) {
          this.dataSource.paginator = this.paginator.getPaginator();
        }
      });
    });
  }

  onAddAccountButtonClicked() {
    this.router.navigate(['/account/create']);
  }

  onRowClicked(row: Account, event: any) {
    if (this.isButton(event)) {
      return;
    }
    this.router.navigate(['/account/' + row.id]);
  }

  getCurrencyName(account: Account): string {
    let currency = CacheService.getCurrencyById(account.currencyId);
    if (currency == null) {
      return '';
    }
    return currency.name;
  }

  isButton(event: any): boolean {
    return (event != null &&
      event.target != null &&
      event.target.classList != null &&
      event.target.classList.length > 0 &&
      event.target.classList[0] == 'mat-mdc-button-touch-target');
  }

  onDeleteAccount(account: Account) {
    this.dialogService.deleteConfirmation().subscribe(() => {
      this.api.deleteAccount(account.id).subscribe(() => {
        this.dialogService.snackbar(
          this.translateService.translate('account/deleted-message'),
          this.translateService.translate('close'));
        this.reset();
      });
    });
  }

  protected readonly CacheService = CacheService;
}
