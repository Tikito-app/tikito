import {Component, Input, ChangeDetectionStrategy} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCheckbox} from "@angular/material/checkbox";
import {TranslatePipe} from "@ngx-translate/core";
import {ImportExportSettings} from "../../dto/import-export-settings";

@Component({
    selector: 'app-admin-import-export-setting',
    imports: [
        FormsModule,
        MatCheckbox,
        ReactiveFormsModule,
        TranslatePipe
    ],
    templateUrl: './admin-import-export-setting.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './admin-import-export-setting.component.scss'
})
export class AdminImportExportSettingComponent {
  accountsDisabled: boolean = false;

  @Input()
  showAccounts: boolean = true;

  form = new FormGroup({
    accounts: new FormControl({value: true, disabled: true}),
    moneyTransactions: new FormControl(),
    moneyTransactionGroups: new FormControl(),
    securityTransactions: new FormControl(),
    loans: new FormControl()
  });

  getSettings(): ImportExportSettings {
    return new ImportExportSettings(
      this.toBoolean(this.form.value.accounts),
      this.toBoolean(this.form.value.moneyTransactions),
      this.toBoolean(this.form.value.moneyTransactionGroups),
      this.toBoolean(this.form.value.securityTransactions),
      this.toBoolean(this.form.value.loans)
    )
  }

  toBoolean(value: any) {
    return value == null ? false : value;
  }
}
