import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButton, MatFabButton} from "@angular/material/button";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatFormField, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";
import {Loan} from "../../dto/loan";
import {LoanInterest} from "../../dto/loan-interest";
import {Account} from "../../dto/account";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {LoanApi} from "../../api/loan-api";
import {AccountApi} from "../../api/account-api";
import {ActivatedRoute, Router} from "@angular/router";
import {MoneyApi} from "../../api/money-api";
import {AuthService} from "../../service/auth.service";
import {Util} from "../../util";
import {UserPreference} from "../../dto/user-preference";
import {UserPreferenceService} from "../../service/user-preference-service";
import {CacheService} from "../../service/cache-service";
import {LoanPartListComponent} from "../loan-part-list/loan-part-list.component";
import {MoneyTransactionGroupType} from "../../dto/money-transaction-group-type";
import {MatSelect} from "@angular/material/select";

@Component({
  selector: 'app-loan-form',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatCard,
    MatCardContent,
    MatCardHeader,
    MatCardTitle,
    MatFabButton,
    MatFormField,
    MatIcon,
    MatInput,
    MatLabel,
    MatSuffix,
    NgIf,
    ReactiveFormsModule,
    TranslatePipe,
    LoanPartListComponent,
    MatOption,
    MatSelect
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './loan-form.component.html',
  styleUrl: './loan-form.component.scss'
})
export class LoanFormComponent implements OnInit {
  form: FormGroup;
  loan: Loan;
  loanId: number;
  interestInEdit: LoanInterest | null;
  nameInEdit: boolean;
  accounts: Account[];
  groups: MoneyTransactionGroup[];


  constructor(private api: LoanApi,
              private accountApi: AccountApi,
              private router: Router,
              private moneyApi: MoneyApi,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.loanId = Util.getIdFromRoute(this.route, 'loanId');
      this.nameInEdit = this.loanId == 0;
      this.accountApi.getAccounts().subscribe(accounts => {
        this.accounts = accounts;
        this.reset();
      });
    });
  }

  reset() {
    let group: any = {
      name: new FormControl(''),
      groupIds: new FormControl(''),
    };
    this.form = new FormGroup(group);

    this.moneyApi.getMoneyTransactionGroups().subscribe(groups => {
      this.groups = groups.filter(group => group.groupTypes.includes(MoneyTransactionGroupType.LOAN));

      if (this.loanId != 0) {
        this.api.getLoan(this.loanId as number).subscribe(loan => {
          this.loan = loan;
          this.form.controls['name'].setValue(loan.name);
          this.form.controls['groupIds'].setValue(this.loan.groups.map(group => group.id));

        });
      } else {
        this.loan = new Loan();
      }
    });
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateLoan(
      this.loanId,
      this.form.value.name,
      this.form.value.groupIds == '' ? [] : this.form.value.groupIds
    ).subscribe(loan => {
      window.location.href = '/loan/' + loan.id;
    })
  }

  onCancelButtonClicked() {
    this.router.navigate(['/loan']);
  }

  onDeleteButtonClicked() {
    if (this.loanId != 0) {
      this.api.deleteLoan(this.loanId).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

  onAddLoanPartClicked() {
    this.router.navigate(['/loan/' + this.loan.id + '/part/create']);
  }

  protected readonly Util = Util;
  protected readonly UserPreference = UserPreference;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly CacheService = CacheService;
}
