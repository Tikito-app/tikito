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
import {MatPaginator} from "@angular/material/paginator";
import {MatIcon} from "@angular/material/icon";
import {MatFabButton} from "@angular/material/button";
import {Router} from "@angular/router";
import {BudgetApi} from "../../api/budget-api";
import Budget from "../../dto/budget";
import {TranslatePipe} from "@ngx-translate/core";
import {NgIf} from "@angular/common";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [
    MatTable,
    MatColumnDef,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatPaginator,
    MatHeaderRowDef,
    MatHeaderCellDef,
    MatCellDef,
    MatRowDef,
    MatIcon,
    MatFabButton,
    TranslatePipe,
    NgIf
  ],
  templateUrl: './budget-list.component.html',
  styleUrl: './budget-list.component.scss'
})
export class BudgetListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name', 'amount', 'date-range', 'groups'];
  dataSource: MatTableDataSource<Budget>;
  budgets: Budget[];

  @ViewChild(MatPaginator) paginator: MatPaginator;


  constructor(
    private router: Router,
    private api: BudgetApi) {
  }

  ngAfterViewInit() {
    this.api.getBudgets().subscribe(budgets => {
      this.budgets = budgets;

      this.dataSource = new MatTableDataSource<Budget>(budgets);
      this.dataSource.paginator = this.paginator;

    })
  }

  onAddBudgetButtonClicked() {
    this.router.navigate(['/budget/create']);
  }

  mapGroups(groups: MoneyTransactionGroup[]): string {
    return groups.map((group: MoneyTransactionGroup) => group.name).join(',');
  }

  onRowClicked(row: Budget) {
    this.router.navigate(['/budget/' + row.id]);
  }
}
