import {Component, Inject, OnInit} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatFormField, MatHint, MatInput, MatLabel, MatSuffix} from "@angular/material/input";
import {TranslatePipe} from "@ngx-translate/core";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatIcon} from "@angular/material/icon";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {CacheService} from "../../service/cache-service";
import {Util} from "../../util";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {NgIf} from "@angular/common";
import {TranslatePipePipe} from "../../service/translate-pipe.pipe";
import {MoneyApi} from "../../api/money-api";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {AccountType} from "../../dto/account-type";
import MoneyTransaction from "../../dto/money/money-transaction";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {Loan} from "../../dto/loan";
import Budget from "../../dto/budget";
import {LoanApi} from "../../api/loan-api";
import {BudgetApi} from "../../api/budget-api";

export interface MyData {
  transaction: MoneyTransaction;
}

@Component({
  selector: 'app-money-transaction-dialog',
  standalone: true,
  imports: [
    MatButton,
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
    MatLabel,
    TranslatePipe,
    FormsModule,
    MatFormField,
    MatIcon,
    MatInput,
    MatOption,
    MatSelect,
    ReactiveFormsModule,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatHint,
    MatSuffix,
    NgIf,
    TranslatePipePipe
  ],
  providers: [provideNativeDateAdapter(), TranslatePipe],
  templateUrl: './create-or-update-money-transaction-dialog.component.html',
  styleUrl: './create-or-update-money-transaction-dialog.component.scss'
})
export class CreateOrUpdateMoneyTransactionDialogComponent implements OnInit {
  form: FormGroup;
  accounts: Account[] = [];
  groups: MoneyTransactionGroup[] = [];
  loans: Loan[] = [];
  budgets: Budget[] = [];

  constructor(
    public dialogRef: MatDialogRef<CreateOrUpdateMoneyTransactionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MyData,
    private api: MoneyApi,
    private loanApi: LoanApi,
    private budgetApi: BudgetApi,
    private accountApi : AccountApi) {
  }

  ngOnInit(): void {
    this.form = new FormGroup({
      accountId: new FormControl(''),
      counterpartAccountName: new FormControl(''),
      counterpartAccountNumber: new FormControl(''),
      amount: new FormControl(''),
      finalBalance: new FormControl(''),
      description: new FormControl(''),
      timestamp: new FormControl(''),
      currencyId: new FormControl(''),
      exchangeRate: new FormControl(''),
      transactionType: new FormControl(''),
      groupId: new FormControl(''),
      loanId: new FormControl(''),
      budgetId: new FormControl(''),
    });

    console.log(this.data.transaction)

    this.form.controls['accountId'].setValue(this.data.transaction.accountId);
    this.form.controls['counterpartAccountName'].setValue(this.data.transaction.counterpartAccountName);
    this.form.controls['counterpartAccountNumber'].setValue(this.data.transaction.counterpartAccountNumber);
    this.form.controls['amount'].setValue(this.data.transaction.amount);
    this.form.controls['finalBalance'].setValue(this.data.transaction.finalBalance);
    this.form.controls['description'].setValue(this.data.transaction.description);
    this.form.controls['timestamp'].setValue(this.data.transaction.timestamp);
    this.form.controls['currencyId'].setValue(this.data.transaction.currencyId);
    this.form.controls['exchangeRate'].setValue(this.data.transaction.exchangeRate);
    this.form.controls['groupId'].setValue(this.data.transaction.groupId);
    this.form.controls['loanId'].setValue(this.data.transaction.loanId);
    this.form.controls['budgetId'].setValue(this.data.transaction.budgetId);

    this.accountApi.getAccounts().subscribe(accounts => {
      this.accounts = accounts.filter(account => account.accountType == AccountType.DEBIT);
    });

    this.api.getMoneyTransactionGroups().subscribe(groups => this.groups = groups)
    this.loanApi.getLoans().subscribe(loans => this.loans = loans)
    this.budgetApi.getBudgets().subscribe(budgets => this.budgets = budgets)
  }

  onSave() {
    this.api.createOrUpdateTransaction(
      this.data.transaction.id,
      this.form.value.accountId,
      this.form.value.counterpartAccountName,
      this.form.value.counterpartAccountNumber,
      this.form.value.timestamp,
      this.form.value.amount,
      this.form.value.finalBalance,
      this.form.value.description,
      this.form.value.currencyId,
      this.form.value.groupId,
      this.form.value.budgetId,
      this.form.value.loanId,
      this.form.value.exchangeRate
    ).subscribe(transaction => {
      this.dialogRef.close(transaction);
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  protected readonly CacheService = CacheService;
  protected readonly Util = Util;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
}
