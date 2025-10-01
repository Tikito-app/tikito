import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
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
import {PaginatorComponent} from "../../components/paginator/paginator.component";
import {MatSort} from "@angular/material/sort";
import {ActivatedRoute, Router} from "@angular/router";
import {LoanApi} from "../../api/loan-api";
import {Loan} from "../../dto/loan";
import {NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";
import {AuthService} from "../../service/auth.service";
import {LoanPart} from "../../dto/loan-part";

@Component({
  selector: 'app-loan-part-list',
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
    MatHeaderCellDef
  ],
  templateUrl: './loan-part-list.component.html',
  styleUrl: './loan-part-list.component.scss'
})
export class LoanPartListComponent implements AfterViewInit {
  displayedColumns: string[] = ['name'];
  dataSource: MatTableDataSource<LoanPart>;
  @Input()
  loan: Loan;

  @ViewChild(PaginatorComponent) paginator: PaginatorComponent;
  @ViewChild(MatSort) sort: MatSort;

  @Input()
  loanParts: LoanPart[];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private api: LoanApi) {
  }

  ngAfterViewInit() {

    // this.authService.onSystemReady((loggedInUser: any) => {
    //   this.loanId = Util.getIdFromRoute(this.route, 'loanId') as number;
    //   console.log(l)
    //   this.api.getLoan(this.loanId).subscribe(loan => {
    //     this.loan = loan;

    this.dataSource = new MatTableDataSource<LoanPart>(this.loanParts);
    setTimeout(() => {
      if (this.paginator) {
        this.dataSource.paginator = this.paginator.getPaginator();
        this.dataSource.sort = this.sort;
      }
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  onAddLoanPartButtonClicked() {
    this.router.navigate(['/loan/' + this.loan.id + '/create']);
  }

  onRowClicked(row: Loan) {
    this.router.navigate(['/loan/' + this.loan.id + '/part/' + row.id]);
  }
}
