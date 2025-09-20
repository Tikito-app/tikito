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
import {Router} from "@angular/router";
import {TranslatePipe} from "@ngx-translate/core";
import {NgIf} from "@angular/common";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import SecurityHolding from "../../dto/security/security-holding";
import {SecurityApi} from "../../api/security-api";
import {SecurityUtil} from "../../security-util";
import {
  SecurityHoldingListDetailsComponent
} from "../security-holding-list-details/security-holding-list-details.component";
import {CurrencyComponent} from "../../components/currency/currency.component";
import {
  AggregatedSecurityHoldingsGraphComponent
} from "../aggregated-security-holdings-graph/aggregated-security-holdings-graph.component";
import {Util} from "../../util";
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {SecurityPieGraphComponent} from "../security-pie-graph/security-pie-graph.component";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatCheckbox, MatCheckboxChange} from "@angular/material/checkbox";
import {MatButton} from "@angular/material/button";
import {MatMenu, MatMenuItem, MatMenuTrigger} from "@angular/material/menu";
import {DialogService} from "../../service/dialog.service";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-security-holding-list',
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
    TranslatePipe,
    NgIf,
    PaginatorComponent,
    SecurityHoldingListDetailsComponent,
    CurrencyComponent,
    AggregatedSecurityHoldingsGraphComponent,
    MatTabGroup,
    MatTab,
    SecurityPieGraphComponent,
    ReactiveFormsModule,
    MatCheckbox,
    MatButton,
    MatMenu,
    MatMenuItem,
    MatMenuTrigger
  ],
  providers: [TranslatePipe],
  templateUrl: './security-holding-list.component.html',
  styleUrl: './security-holding-list.component.scss'
})
export class SecurityHoldingListComponent implements AfterViewInit {
  displayedColumns: string[] = ['security-name', 'security-type', 'position-value', 'isin', 'performance', 'profit-loss', 'options'];
  dataSource: MatTableDataSource<SecurityHolding>;
  allHoldings: SecurityHolding[];
  selectedHolding: SecurityHolding | null;

  form: FormGroup;

  @ViewChild(PaginatorComponent)
  paginator: PaginatorComponent;

  constructor(
    private router: Router,
    private dialogService: DialogService,
    private translate: TranslatePipe,
    private authService: AuthService,
    private api: SecurityApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {

      this.form = new FormGroup({
        showClosedPositions: new FormControl(),
      });
      this.form.controls['showClosedPositions'].setValue(UserPreferenceService.get(UserPreference.SHOW_CLOSED_POSITIONS, true));

      this.reset();
    });
  }

  reset(): void {
    this.api.getSecurityHoldings().subscribe(holdings => {
      this.allHoldings = holdings;

      this.dataSource = new MatTableDataSource<SecurityHolding>(this.filterHoldings(holdings));
      setTimeout(() => {
        this.dataSource.paginator = this.paginator.getPaginator();
      });
    });
  }

  onRowClicked(holding: SecurityHolding, event: any) {
    if (this.isButton(event)) {
      return;
    }
    this.selectedHolding = holding;
  }

  isButton(event: any): boolean {
    return (event != null &&
      event.target != null &&
      event.target.classList != null &&
      event.target.classList.length > 0 &&
      event.target.classList[0] == 'mat-mdc-button-touch-target');
  }

  onRowDblClicked(holding: SecurityHolding, event: any) {
    if (this.isButton(event)) {
      return;
    }
    this.router.navigate(['/security-holding'], {fragment: 'holdingIds=' + holding.id});
  }

  mapIsin(row: SecurityHolding): string {
    return row
      .security
      .isins
      .map(i => i.isin)
      .join(', ');
  }

  onDeleteHolding(holding: SecurityHolding) {
    this.dialogService.okCancel(
      this.translate.transform('are-you-sure-delete-title'),
      this.translate.transform('are-you-sure-delete-text'))
      .then((doDelete) => {
        if (doDelete) {
          this.api.deleteSecurityHolding(holding.id).subscribe(() => this.dialogService.snackbar(
            this.translate.transform('security/holding/deleted-message'),
            this.translate.transform('close')));
        }
      });
  }

  onShowClosedPositionChanged(event: MatCheckboxChange) {
    UserPreferenceService.onCheckboxChange(UserPreference.SHOW_CLOSED_POSITIONS, event.checked);
    this.reset();
  }

  filterHoldings(holdings: SecurityHolding[]) {
    let showClosedPositions = this.form.value['showClosedPositions'];
    return this
      .allHoldings
      .filter(holding => showClosedPositions || holding.amount > 0);
  }

  protected readonly SecurityUtil = SecurityUtil;
  protected readonly Util = Util;
}
