import {AfterViewInit, Component, ViewChild} from '@angular/core';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatNoDataRow,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import {MatFabButton} from "@angular/material/button";
import {ActivatedRoute, Router} from "@angular/router";
import {TranslatePipe} from "@ngx-translate/core";
import {NgIf} from "@angular/common";
import {MoneyApi} from "../../api/money-api";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatSort} from "@angular/material/sort";
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-moneyTransactionGroup-list',
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
    MatFormField,
    MatInput,
    MatNoDataRow,
    MatLabel,
    PaginatorComponent
  ],
  templateUrl: './money-transaction-group-list.component.html',
  styleUrl: './money-transaction-group-list.component.scss'
})
export class MoneyTransactionGroupListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name'];
  dataSource: MatTableDataSource<MoneyTransactionGroup>;
  moneyTransactionGroups: MoneyTransactionGroup[] = [];
  accountId: number;

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private api: MoneyApi) {
  }

  ngAfterViewInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.api.getMoneyTransactionGroups().subscribe(moneyTransactionGroups => {
        this.moneyTransactionGroups = moneyTransactionGroups;

        this.dataSource = new MatTableDataSource<MoneyTransactionGroup>(moneyTransactionGroups);
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

  onAddMoneyTransactionGroupButtonClicked() {
    this.router.navigate(['/money/transaction-group/create']);
  }

  onRowClicked(row: MoneyTransactionGroup) {
    this.router.navigate(['/money/transaction-group/' + row.id]);
  }
}
