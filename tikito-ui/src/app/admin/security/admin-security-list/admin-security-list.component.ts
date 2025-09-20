import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {MatButton} from "@angular/material/button";
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
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {NgIf} from "@angular/common";
import {PaginatorComponent} from "../../../components/paginator/paginator.component";
import {TranslatePipe} from "@ngx-translate/core";
import {Router} from "@angular/router";
import {AuthService} from "../../../service/auth.service";
import {DialogService} from "../../../service/dialog.service";
import {Security} from "../../../dto/security/security";
import {AdminApi} from "../../../api/admin-api";
import {CacheService} from "../../../service/cache-service";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {SecurityType} from "../../../dto/security/security-type";

@Component({
  selector: 'app-admin-security-list',
  standalone: true,
  imports: [
    MatButton,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatIcon,
    MatMenu,
    MatMenuItem,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf,
    PaginatorComponent,
    TranslatePipe,
    MatMenuTrigger,
    MatHeaderCellDef,
    MatFormField,
    MatInput,
    MatLabel
  ],
  providers: [TranslatePipe],
  templateUrl: './admin-security-list.component.html',
  styleUrl: './admin-security-list.component.scss'
})
export class AdminSecurityListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'currency', 'security-type', 'sector', 'industry', 'exchange', 'options'];
  dataSource: MatTableDataSource<Security>;
  securities: Security[];

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;

  constructor(
    private router: Router,
    private authService: AuthService,
    private translate: TranslatePipe,
    private dialogService: DialogService,
    private api: AdminApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.api.getSecurities().subscribe(securities => {
        this.securities = securities;

        this.dataSource = new MatTableDataSource<Security>(securities);
        setTimeout(() => {
          this.dataSource.paginator = this.paginator.getPaginator();
        });
      });
    });
  }

  onRowClicked(row: Security, event: any) {
    if (this.isButton(event)) {
      return;
    }
    this.router.navigate(['/admin/security/' + row.id]);
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

  isButton(event: any): boolean {
    return (event != null &&
      event.target != null &&
      event.target.classList != null &&
      event.target.classList.length > 0 &&
      event.target.classList[0] == 'mat-mdc-button-touch-target');
  }

  onDeleteSecurity(security: Security) {
    this.dialogService.okCancel(
      this.translate.transform('are-you-sure-delete-title'),
      this.translate.transform('are-you-sure-delete-text'))
      .then((doDelete) => {
        if (doDelete) {
          this.api.deleteSecurity(security.id).subscribe(() => this.dialogService.snackbar(
            this.translate.transform('admin/security/deleted-message'),
            this.translate.transform('close')));
        }
      });
  }

  onEditIsin(security: Security) {
    this.router.navigate(['/admin/security/' + security.id + '/isin']);
  }

  routeToAdmin() {
    this.router.navigate(['/admin']);
  }

  onRecalculateHistoricalSecurityValue(security: Security) {
    this.api.recalculateHistoricalSecurityValue(security.id).subscribe(() => {
      this.dialogService.snackbar(this.translate.transform('done'), this.translate.transform('done'));
    });
  }

  onUpdateSecurityPrices(security: Security) {
    this.api.updateSecurityPrices(security.id).subscribe(() => {
      this.dialogService.snackbar(this.translate.transform('done'), this.translate.transform('done'));
    });
  }

  onEnrichSecurity(security: Security) {
    this.api.enrichSecurity(security.id).subscribe(() => {
      this.dialogService.snackbar(this.translate.transform('done'), this.translate.transform('done'));
    });
  }

  onDeleteSecurityPrices(security: Security) {
    this.api.deleteSecurityPrices(security.id).subscribe(() => {
      this.dialogService.snackbar(this.translate.transform('done'), this.translate.transform('done'));
    });
  }

  onUpdateAllClicked() {
    this.api.updateAllSecurities().subscribe(() => {
      this.dialogService.snackbar(this.translate.transform('done'), this.translate.transform('done'));
    });
  }

  protected readonly SecurityType = SecurityType;
}
