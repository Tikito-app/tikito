import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButton, MatFabButton} from "@angular/material/button";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatFormField, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {NgForOf, NgIf} from "@angular/common";
import {TranslatePipe} from "@ngx-translate/core";
import {Account} from "../../dto/account";
import {AccountApi} from "../../api/account-api";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../service/auth.service";
import {Util} from "../../util";
import {LoanApi} from "../../api/loan-api";
import {LoanInterest} from "../../dto/loan-interest";
import {Loan} from "../../dto/loan";
import {LoanInterestListItemComponent} from "../loan-interest-list-item/loan-interest-list-item.component";
import {LoanInterestFormComponent} from "../loan-interest-form/loan-interest-form.component";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {UserPreference} from "../../dto/user-preference";
import {UserPreferenceService} from "../../service/user-preference-service";
import {CacheService} from "../../service/cache-service";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {MoneyTransactionGroupType} from "../../dto/money-transaction-group-type";
import {MoneyApi} from "../../api/money-api";
import {LoanPart} from "../../dto/loan-part";
import {DialogService} from "../../service/dialog.service";

@Component({
  selector: 'app-loan-part-form',
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
    MatOption,
    MatSelect,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    TranslatePipe,
    LoanInterestListItemComponent,
    LoanInterestFormComponent,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatHint,
    MatSuffix
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './loan-part-form.component.html',
  styleUrl: './loan-part-form.component.scss'
})
export class LoanPartFormComponent implements OnInit {
  form: FormGroup;
  loan: Loan;
  loanPart: LoanPart;
  loanId: number;
  loanPartId: number;
  interestInEdit: LoanInterest | null;
  nameInEdit: boolean;
  accounts: Account[];
  groups: MoneyTransactionGroup[];


  constructor(private api: LoanApi,
              private accountApi: AccountApi,
              private router: Router,
              private moneyApi: MoneyApi,
              private authService: AuthService,
              private route: ActivatedRoute,
              private dialogService: DialogService) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.loanId = Util.getIdFromRoute(this.route, 'loanId');
      this.loanPartId = Util.getIdFromRoute(this.route, 'loanPartId');
      this.accountApi.getAccounts().subscribe(accounts => {
        this.accounts = accounts;
        this.reset();
      });
    });
  }

  reset() {
    let group: any = {
      name: new FormControl(''),
      startDate: new FormControl(''),
      endDate: new FormControl(''),
      currencyId: new FormControl(''),
      amount: new FormControl(''),
      loanType: new FormControl(''),
      groupIds: new FormControl(''),
    };
    this.form = new FormGroup(group);
    this.form.controls['currencyId'].setValue(UserPreferenceService.get<string>(UserPreference.DEFAULT_CURRENCY, 'EUR'));

    this.moneyApi.getMoneyTransactionGroups().subscribe(groups => {
      this.groups = groups.filter(group => group.groupTypes.includes(MoneyTransactionGroupType.LOAN));
      this.api.getLoan(this.loanId as number).subscribe(loan => {
        this.loan = loan;
        if (this.loanPartId != 0) {
          this.loanPart = loan.loanParts.filter(part => part.id == this.loanPartId)[0];
          if (this.loanPart != null) {
            this.form.controls['name'].setValue(this.loanPart.name);
            this.form.controls['startDate'].setValue(this.loanPart.startDate);
            this.form.controls['endDate'].setValue(this.loanPart.endDate);
            this.form.controls['currencyId'].setValue(this.loanPart.currencyId);
            this.form.controls['amount'].setValue(this.loanPart.amount);
            this.form.controls['loanType'].setValue(this.loanPart.loanType);
          }
        } else {
          this.loanPart = new LoanPart();
        }

      });
    });
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateLoanPart(
      this.loanPartId,
      this.loan.id,
      this.form.value.name,
      Util.toLocalDate(this.form.value.startDate),
      Util.toLocalDate(this.form.value.endDate),
      this.form.value.amount,
      this.form.value.currencyId,
      this.form.value.loanType,
      this.loanPart.interests
    ).subscribe(loanPart => {
      window.location.href = '/loan/' + this.loan.id + '/part/' + loanPart.id;
    })
  }

  onCancelButtonClicked() {
    this.router.navigate(['/loan/' + this.loan.id]);
  }

  onDeleteButtonClicked() {
    if (this.loanId != 0) {
      this.dialogService.deleteConfirmation().subscribe(() => {
        this.api.deleteLoanPart(this.loanId, this.loanPartId as number).subscribe(() => this.onCancelButtonClicked());
      });
    } else {
      this.onCancelButtonClicked();
    }
  }

  isInterestInEdit(interest: LoanInterest) {
    return this.interestInEdit != null &&
      this.interestInEdit.id == interest.id &&
      this.interestInEdit.startDate == interest.startDate &&
      this.interestInEdit.endDate == interest.endDate &&
      this.interestInEdit.amount == interest.amount;
  }

  onEditInterestButtonClicked(interest: LoanInterest) {
    this.interestInEdit = interest;
  }

  onAddInterestButtonClicked() {
    let interest = new LoanInterest()
    this.interestInEdit = interest;
    this.loanPart.interests.push(interest)
  }

  interestCallback(interest: LoanInterest | null) {
    this.onSaveButtonClicked();
    this.interestInEdit = null;
  }

  interestDeleteCallback() {
    this.api.deleteInterest(this.loanId, this.loanPartId, this.interestInEdit?.id).subscribe(() => this.onCancelButtonClicked());

  }

  editInterestCallback(interest: LoanInterest) {
    this.interestInEdit = interest;
  }

  protected readonly Util = Util;
  protected readonly UserPreference = UserPreference;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly CacheService = CacheService;
}
