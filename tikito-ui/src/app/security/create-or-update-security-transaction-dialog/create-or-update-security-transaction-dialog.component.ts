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
import {SecurityTransaction} from "../../dto/security/security-transaction";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatIcon} from "@angular/material/icon";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {CacheService} from "../../service/cache-service";
import {Util} from "../../util";
import {UserPreferenceService} from "../../service/user-preference-service";
import {UserPreference} from "../../dto/user-preference";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";

import {SecurityApi} from "../../api/security-api";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {TranslatePipe} from "@ngx-translate/core";

export interface MyData {
  transaction: SecurityTransaction;
}

@Component({
    selector: 'app-security-transaction-dialog',
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
    templateUrl: './create-or-update-security-transaction-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './create-or-update-security-transaction-dialog.component.scss'
})
export class CreateOrUpdateSecurityTransactionDialogComponent implements OnInit {
  form: FormGroup;
  accounts: Account[] = [];

  constructor(
    public dialogRef: MatDialogRef<CreateOrUpdateSecurityTransactionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MyData,
    private api: SecurityApi,
    private accountApi : AccountApi) {
  }

  ngOnInit(): void {
    this.form = new FormGroup({
      accountId: new FormControl('', Validators.required),
      isin: new FormControl(''),
      currencyId: new FormControl('', Validators.required),
      amount: new FormControl('', Validators.required),
      price: new FormControl('', Validators.required),
      description: new FormControl(''),
      timestamp: new FormControl('', Validators.required),
      cash: new FormControl(''),
      exchangeRate: new FormControl('', Validators.required),
      transactionType: new FormControl('', Validators.required),
    });

    this.form.controls['accountId'].setValue(this.data.transaction.accountId);
    this.form.controls['isin'].setValue(this.data.transaction.isin);
    this.form.controls['currencyId'].setValue(this.data.transaction.currencyId);
    this.form.controls['amount'].setValue(this.data.transaction.amount);
    this.form.controls['price'].setValue(this.data.transaction.price);
    this.form.controls['description'].setValue(this.data.transaction.description);
    this.form.controls['timestamp'].setValue(this.data.transaction.timestamp);
    this.form.controls['cash'].setValue(this.data.transaction.cash);
    this.form.controls['exchangeRate'].setValue(this.data.transaction.exchangeRate);
    this.form.controls['transactionType'].setValue(this.data.transaction.transactionType);

    this.accountApi.getAccounts().subscribe(accounts => this.accounts = accounts);
  }

  onSave() {
    if(this.form.invalid) {
      return;
    }
    this.api.createOrUpdateTransaction(
      this.data.transaction.id,
      this.form.value.accountId,
      this.form.value.isin,
      this.form.value.currencyId,
      this.form.value.amount,
      this.form.value.price,
      this.form.value.description,
      this.form.value.timestamp,
      this.form.value.cash,
      this.form.value.exchangeRate,
      this.form.value.transactionType,
      ).subscribe(transaction => {
      this.dialogRef.close(transaction);
    })
  }

  onCancel() {
    this.dialogRef.close();
  }

  protected readonly CacheService = CacheService;
  protected readonly Util = Util;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
}
