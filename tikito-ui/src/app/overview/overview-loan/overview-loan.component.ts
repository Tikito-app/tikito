import {Component, Input} from '@angular/core';
import {Loan} from "../../dto/loan";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {TranslatePipe} from "@ngx-translate/core";
import {LoanValue} from "../../dto/loan-value";
import {CurrencyComponent} from "../../components/currency/currency.component";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-overview-loan',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    TranslatePipe,
    CurrencyComponent,
    NgIf
  ],
  templateUrl: './overview-loan.component.html',
  styleUrl: './overview-loan.component.scss'
})
export class OverviewLoanComponent {
  @Input()
  loan: Loan;

  @Input()
  value: LoanValue;
}
