import {Component, Inject, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import {MatError, MatFormField, MatHint, MatInput, MatLabel, MatSuffix} from "@angular/material/input";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatIcon} from "@angular/material/icon";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {CacheService} from "../../service/cache-service";
import {Util} from "../../util";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";

import {MoneyApi} from "../../api/money-api";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import MoneyTransaction from "../../dto/money/money-transaction";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {Loan} from "../../dto/loan";
import {LoanApi} from "../../api/loan-api";
import {TranslatePipe, TranslateService} from "@ngx-translate/core";
import {DialogService} from "../../service/dialog.service";

export interface MyData {
  transaction: MoneyTransaction;
}

@Component({
    selector: 'app-money-transaction-dialog',
    imports: [
    MatButton,
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
    MatLabel,
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
    TranslatePipe,
    MatError
],
    providers: [provideNativeDateAdapter()],
    templateUrl: './create-or-update-money-transaction-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './create-or-update-money-transaction-dialog.component.scss'
})
export class CreateOrUpdateMoneyTransactionDialogComponent implements OnInit {
  form: FormGroup;
  accounts: Account[] = [];
  groups: MoneyTransactionGroup[] = [];
  loans: Loan[] = [];

  constructor(
    public dialogRef: MatDialogRef<CreateOrUpdateMoneyTransactionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MyData,
    private api: MoneyApi,
    private loanApi: LoanApi,
    private dialogService: DialogService,
    private translateService: TranslateService,
    private accountApi: AccountApi) {
  }

  ngOnInit(): void {
    this.form = new FormGroup({
      accountId: new FormControl('', Validators.required),
      counterpartyAccountName: new FormControl(''),
      counterpartyAccountNumber: new FormControl(''),
      amount: new FormControl('', Validators.required),
      finalBalance: new FormControl(''),
      description: new FormControl(''),
      timestamp: new FormControl('', Validators.required),
      currencyId: new FormControl('', Validators.required),
      exchangeRate: new FormControl('', Validators.required),
      transactionType: new FormControl('', Validators.required),
      groupId: new FormControl(''),
      loanId: new FormControl(''),
    });

    this.form.controls['accountId'].setValue(this.data.transaction.accountId);
    this.form.controls['counterpartyAccountName'].setValue(this.data.transaction.counterpartyAccountName);
    this.form.controls['counterpartyAccountNumber'].setValue(this.data.transaction.counterpartyAccountNumber);
    this.form.controls['amount'].setValue(this.data.transaction.amount);
    this.form.controls['finalBalance'].setValue(this.data.transaction.finalBalance);
    this.form.controls['description'].setValue(this.data.transaction.description);
    this.form.controls['timestamp'].setValue(this.data.transaction.timestamp);
    this.form.controls['currencyId'].setValue(this.data.transaction.currencyId);
    this.form.controls['exchangeRate'].setValue(this.data.transaction.exchangeRate);
    this.form.controls['groupId'].setValue(this.data.transaction.groupId);
    this.form.controls['loanId'].setValue(this.data.transaction.loanId);

    this.accountApi.getAccounts().subscribe(accounts => {
      this.accounts = accounts;
    });

    this.api.getMoneyTransactionGroups().subscribe(groups => this.groups = groups)
    this.loanApi.getLoans().subscribe(loans => this.loans = loans)
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateTransaction(
      this.data.transaction.id,
      this.form.value.accountId,
      this.form.value.counterpartyAccountName,
      this.form.value.counterpartyAccountNumber,
      this.form.value.timestamp,
      this.form.value.amount,
      this.form.value.finalBalance,
      this.form.value.description,
      this.form.value.currencyId,
      this.form.value.groupId,
      this.form.value.loanId,
      this.form.value.exchangeRate
    ).subscribe(transaction => {
      this.dialogRef.close(transaction);
    });
  }

  onCancelButtonClicked() {
    this.dialogRef.close();
  }

  onDeleteButtonClicked() {
    this.dialogService.deleteConfirmation().subscribe(() => {
      this.api.deleteMoneyTransaction(this.data.transaction.id).subscribe(() => this.dialogService.snackbar(
        this.translateService.instant('money/transaction/deleted-message'),
        this.translateService.instant('close')));
      this.dialogRef.close();
    });
  }

  protected readonly CacheService = CacheService;
  protected readonly Util = Util;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
}
