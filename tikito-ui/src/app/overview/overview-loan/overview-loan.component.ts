import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {Loan} from "../../dto/loan";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {LoanValue} from "../../dto/loan-value";
import {CurrencyComponent} from "../../components/currency/currency.component";

import {TranslatePipe} from "@ngx-translate/core";

@Component({
    selector: 'app-overview-loan',
    imports: [
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    CurrencyComponent,
    TranslatePipe
],
    templateUrl: './overview-loan.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './overview-loan.component.scss'
})
export class OverviewLoanComponent {
  @Input()
  loan: Loan;

  @Input()
  value: LoanValue;
}
