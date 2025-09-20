import {Component, Input, OnInit, ViewChild} from '@angular/core';
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
import {DatePipe, NgIf} from "@angular/common";
import {PaginatorComponent} from "../../../components/paginator/paginator.component";
import {TranslatePipe} from "@ngx-translate/core";
import {CurrencyComponent} from "../../../components/currency/currency.component";
import {Util} from "../../../util";
import {MatSort} from "@angular/material/sort";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MoneyTransactionImportLine} from "../../../dto/money/money-transaction-import-line";

@Component({
  selector: 'app-money-import-result',
  standalone: true,
  imports: [
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf,
    PaginatorComponent,
    TranslatePipe,
    MatHeaderCellDef,
    CurrencyComponent,
    DatePipe,
    MatFormField,
    MatInput,
    MatLabel
  ],
  templateUrl: './money-import-result.component.html',
  styleUrl: './money-import-result.component.scss'
})
export class MoneyImportResultComponent implements OnInit {
  @Input()
  transactions: MoneyTransactionImportLine[];

  displayedColumns: string[] = ['row', 'timestamp', 'counterpart-account-name', 'counterpart-account-number', 'amount', 'description', 'exchange-rate', 'failed-reason'];
  dataSource: MatTableDataSource<MoneyTransactionImportLine>;

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource<MoneyTransactionImportLine>(this.transactions);

    setTimeout(() => {
      this.dataSource.paginator = this.paginator.getPaginator();
      this.dataSource.sort = this.sort
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  protected readonly Util = Util;
}
