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
import {MatFabButton} from "@angular/material/button";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {NgIf} from "@angular/common";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {TranslatePipe} from "@ngx-translate/core";
import {Loan} from "../../dto/loan";
import {MatSort} from "@angular/material/sort";
import {ActivatedRoute, Router} from "@angular/router";
import {LoanApi} from "../../api/loan-api";
import {MatTab, MatTabGroup} from "@angular/material/tabs";
import {LoanGraphComponent} from "../loan-graph/loan-graph.component";
import {LoanGraphFilterComponent} from "../loan-graph-filter/loan-graph-filter.component";
import {LoanValue} from "../../dto/loan-value";
import {
  LoanMoneyTransactionsListComponent
} from "../loan-money-transactions-list/loan-money-transactions-list.component";

@Component({
  selector: 'app-loan-list',
  standalone: true,
  imports: [
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatFabButton,
    MatFormField,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatIcon,
    MatInput,
    MatLabel,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf,
    PaginatorComponent,
    TranslatePipe,
    MatHeaderCellDef,
    MatTab,
    MatTabGroup,
    LoanGraphComponent,
    LoanGraphFilterComponent,
    LoanMoneyTransactionsListComponent
  ],
  templateUrl: './loan-list.component.html',
  styleUrl: './loan-list.component.scss'
})
export class LoanListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name'];
  dataSource: MatTableDataSource<Loan>;
  loans: Loan[] = [];
  accountId: number;
  tabIndex: number;
  allValues: LoanValue[] = [];

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private api: LoanApi) {
  }

  ngAfterViewInit() {
    this.api.getLoans().subscribe(loans => {
      this.loans = loans;
      this.api.getLoanValues().subscribe(values => {
        this.allValues = values;
        this.dataSource = new MatTableDataSource<Loan>(loans);
        setTimeout(() => {
          if (this.paginator) {
            this.dataSource.paginator = this.paginator.getPaginator();
            this.dataSource.sort = this.sort;
          }
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

  onAddLoanButtonClicked() {
    this.router.navigate(['/loan/create']);
  }

  onRowClicked(row: Loan) {
    this.router.navigate(['/loan/' + row.id]);
  }

  onTabSelectionChanged(tabIndex: number) {
    this.tabIndex = tabIndex;
  }
}
