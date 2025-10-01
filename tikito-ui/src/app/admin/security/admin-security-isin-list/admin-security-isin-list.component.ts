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
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../service/auth.service";
import {TranslatePipe} from "@ngx-translate/core";
import {DialogService} from "../../../service/dialog.service";
import {AdminApi} from "../../../api/admin-api";
import {Isin} from "../../../dto/isin";
import {Util} from "../../../util";
import {MatButton} from "@angular/material/button";
import {MatIcon} from "@angular/material/icon";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {NgIf} from "@angular/common";
import {TranslateService} from "../../../service/translate.service";

@Component({
  selector: 'app-admin-security-isin-list',
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
    MatHeaderCellDef
  ],
  providers: [TranslatePipe],
  templateUrl: './admin-security-isin-list.component.html',
  styleUrl: './admin-security-isin-list.component.scss'
})
export class AdminSecurityIsinListComponent implements AfterViewInit {
  displayedColumns: string[] = ['isin', 'symbol', 'valid-from', 'valid-to'];
  dataSource: MatTableDataSource<Isin>;
  security: Security;

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;


  constructor(
    private router: Router,
    private authService: AuthService,
    private translateService: TranslateService,
    private dialogService: DialogService,
    private route: ActivatedRoute,
    private api: AdminApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      let securityId = Util.getIdFromRoute(this.route, 'securityId') as number;

      this.api.getSecurity(securityId).subscribe(security => {
        this.security = security;
        this.api.getIsins(securityId).subscribe(isins => {

          this.dataSource = new MatTableDataSource<Isin>(isins);
          setTimeout(() => {
            this.dataSource.paginator = this.paginator.getPaginator();
          });
        });
      });
    });
  }

  onRowClicked(row: Isin, event: any) {
    if (this.isButton(event)) {
      return;
    }
    this.router.navigate(['/admin/security/' + this.security.id + '/isin/' + row.isin]);
  }

  isButton(event: any): boolean {
    return (event != null &&
      event.target != null &&
      event.target.classList != null &&
      event.target.classList.length > 0 &&
      event.target.classList[0] == 'mat-mdc-button-touch-target');
  }

  onDeleteIsin(isin: Isin) {
    this.dialogService.deleteConfirmation().subscribe(() => {
      this.api.deleteIsin(isin.isin).subscribe(() => this.dialogService.snackbar(
        this.translateService.translate('admin/security/isin/deleted-message'),
        this.translateService.translate('close')));
    });
  }

  routeToSecurities() {
    this.router.navigate(['/admin/security']);
  }
}
