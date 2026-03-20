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
import {SecurityTransaction} from "../../dto/security/security-transaction";
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
import {SecurityApi} from "../../api/security-api";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {AccountType} from "../../dto/account-type";

export interface MyData {
  transaction: SecurityTransaction;
}

@Component({
  selector: 'app-security-transaction-dialog',
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
  templateUrl: './create-or-edit-security-transaction-dialog.component.html',
  styleUrl: './create-or-edit-security-transaction-dialog.component.scss'
})
export class CreateOrEditSecurityTransactionDialogComponent implements OnInit {
  form: FormGroup;
  accounts: Account[] = [];

  constructor(
    public dialogRef: MatDialogRef<CreateOrEditSecurityTransactionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: MyData,
    private api: SecurityApi,
    private accountApi : AccountApi) {
  }

  ngOnInit(): void {
    this.form = new FormGroup({
      accountId: new FormControl(''),
      isin: new FormControl(''),
      currencyId: new FormControl(''),
      amount: new FormControl(''),
      price: new FormControl(''),
      description: new FormControl(''),
      timestamp: new FormControl(''),
      cash: new FormControl(''),
      exchangeRate: new FormControl(''),
      transactionType: new FormControl(''),
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

    this.accountApi.getAccounts().subscribe(accounts => {
      this.accounts = accounts.filter(account => account.accountType == AccountType.SECURITY);
    });
  }

  onSave() {
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
