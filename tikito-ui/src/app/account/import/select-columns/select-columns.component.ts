import {Component, Input, OnInit} from '@angular/core';
import {NgIf} from "@angular/common";
import {MatOption} from "@angular/material/core";
import {MatFormField, MatLabel, MatSelect} from "@angular/material/select";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "@ngx-translate/core";
import {Util} from "../../../util";
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {MatInput} from "@angular/material/input";
import {ImportFileProcessState} from "../../../dto/import-file-process-state";
import {AccountType} from "../../../dto/account-type";
import {UserPreferenceService} from "../../../service/user-preference-service";
import {UserPreference} from "../../../dto/user-preference";

@Component({
  selector: 'app-select-columns',
  standalone: true,
  imports: [
    MatOption,
    MatSelect,
    ReactiveFormsModule,
    TranslatePipe,
    MatFormField,
    MatLabel,
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf,
    MatHeaderCellDef,
    MatInput
  ],
  templateUrl: './select-columns.component.html',
  styleUrl: './select-columns.component.scss'
})
export class SelectColumnsComponent implements OnInit {

  @Input() data: string[];
  @Input() state: ImportFileProcessState;

  requiredColumns: string[];
  columns: string[];
  displayBuySellValueFieldOnColumn: number;
  displayCreditDebitValueFieldOnColumn: number;
  displayTimestampFormatFieldOnColumn: number;
  displayDateFormatFieldOnColumn: number;
  displayTimeFormatFieldOnColumn: number;

  form: FormGroup;
  dataSource: MatTableDataSource<any>;

  securityColumns = ['date', 'isin', 'amount', 'price', 'transaction-costs', 'admin-costs', 'currency', 'exchange-rate', 'buy-sell', 'buy-text', 'description'];
  moneyColumns = ['timestamp', 'counterpart-account-name', 'counterpart-account-number', 'amount', 'debit-credit', 'currency', 'exchange-rate', 'final-balance', 'description'];


  ngOnInit(): void {
    let group: any = {};

    if (this.state.accountType == AccountType.SECURITY) {
      this.requiredColumns = this.securityColumns;
    } else {
      this.requiredColumns = this.moneyColumns;
    }

    this.columns = [];
    for (let i = 0; i < this.state.parsedContent[0].length; i++) {
      this.columns.push('column-' + i);
      group['column-' + i] = new FormControl();
    }

    group['buy-value'] = new FormControl();
    group['timestamp-format'] = new FormControl();
    group['date-format'] = new FormControl();
    group['time-format'] = new FormControl();

    this.form = new FormGroup(group);
    this.dataSource = new MatTableDataSource<any>(this.state.parsedContent);

    this.form.controls['date-format'].setValue(UserPreferenceService.get(UserPreference.IMPORT_DATE_FORMAT, 'dd-MM-yyyy'));
  }

  getHeaderConfig() {
    let config: any = {};
    for (let i = 0; i < this.state.parsedContent[0].length; i++) {
      let value = this.form.value['column-' + i];

      if (value != '' && value != null) {
        config[value] = i;
      }
    }
    return config;
  }

  getBuyValue() {
    if (this.displayBuySellValueFieldOnColumn) {
      return this.form.value['buy-value'];
    }
    return null;
  }

  getDebitCreditValue() {
    if (this.displayCreditDebitValueFieldOnColumn) {
      return this.form.value['debit-credit-value'];
    }
    return null;
  }

  getTimestampFormatValue() {
    if (this.displayTimestampFormatFieldOnColumn) {
      return this.form.value['timestamp-format'];
    }
    return null;
  }

  getDateFormatValue() {
    if (this.displayDateFormatFieldOnColumn) {
      return this.form.value['date-format'];
    }
    return null;
  }

  getTimeFormatValue() {
    if (this.displayTimeFormatFieldOnColumn) {
      return this.form.value['time-format'];
    }
    return null;
  }

  onColumnChanged() {
    for (let i = 0; i < this.state.parsedContent[0].length; i++) {
      if (this.form.value['column-' + i] == 'buy-sell') {
        this.displayBuySellValueFieldOnColumn = i;
      } else if (this.form.value['column-' + i] == 'debit-credit') {
        this.displayCreditDebitValueFieldOnColumn = i;
      } else if (this.form.value['column-' + i] == 'timestamp') {
        this.displayTimestampFormatFieldOnColumn = i;
      } else if (this.form.value['column-' + i] == 'date') {
        this.displayDateFormatFieldOnColumn = i;
      } else if (this.form.value['column-' + i] == 'time') {
        this.displayTimeFormatFieldOnColumn = i;
      }
    }
  }

  protected readonly Util = Util;
  protected readonly UserPreferenceService = UserPreferenceService;
  protected readonly UserPreference = UserPreference;
}
