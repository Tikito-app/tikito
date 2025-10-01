import {Component} from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCheckbox} from "@angular/material/checkbox";
import {TranslatePipe} from "@ngx-translate/core";

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

    form = new FormGroup({
        accounts: new FormControl({value: true, disabled: true}),
        moneyTransactionGroups: new FormControl(),
        moneyTransactions: new FormControl(),
        securityTransactions: new FormControl(),
        loans: new FormControl()
    });

    setAccountChecked() {
        // if (this.form.value.moneyTransactionGroups ||
        //     this.form.value.moneyTransactions ||
        //     this.form.value.securityTransactions ||
        //     this.form.value.loans) {
        //
        //     this.accountsDisabled = true;
        //     this.form.controls['accounts'].setValue(true);
        // } else {
        //     this.accountsDisabled = false;
        // }
    }
}
