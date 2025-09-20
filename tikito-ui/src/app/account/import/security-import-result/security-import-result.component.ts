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
import {PaginatorComponent} from "../../../components/paginator/paginator.component";
import {MatSort} from "@angular/material/sort";
import {Util} from "../../../util";
import {CurrencyComponent} from "../../../components/currency/currency.component";
import {DatePipe, NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";
import {SecurityTransactionImportLine} from "../../../dto/security/security-transaction-import-line";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";

@Component({
  selector: 'app-security-import-result',
  standalone: true,
  imports: [
    CurrencyComponent,
    DatePipe,
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
    MatFormField,
    MatInput,
    MatLabel
  ],
  templateUrl: './security-import-result.component.html',
  styleUrl: './security-import-result.component.scss'
})
export class SecurityImportResultComponent implements OnInit {

  @Input()
  transactions: SecurityTransactionImportLine[];

  displayedColumns: string[] = ['row', 'date', 'isin', 'product', 'amount', 'price', 'currency', 'exchange-rate', 'description', 'failed-reason'];
  dataSource: MatTableDataSource<SecurityTransactionImportLine>;

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource<SecurityTransactionImportLine>(this.transactions);

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
