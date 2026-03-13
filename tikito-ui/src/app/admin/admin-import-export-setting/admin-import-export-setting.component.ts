import {Component, Input} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCheckbox} from "@angular/material/checkbox";
import {TranslatePipe} from "@ngx-translate/core";
import {ImportExportSettings} from "../../dto/import-export-settings";

@Component({
  selector: 'app-admin-import-export-setting',
  standalone: true,
  imports: [
    FormsModule,
    MatCheckbox,
    ReactiveFormsModule,
    TranslatePipe
  ],
  templateUrl: './admin-import-export-setting.component.html',
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
      this.form.value.accounts as boolean,
      this.form.value.moneyTransactions,
      this.form.value.moneyTransactionGroups,
      this.form.value.securityTransactions,
      this.form.value.loans,
    )
  }
}
