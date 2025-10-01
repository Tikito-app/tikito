import {Component, OnInit, ViewChild} from '@angular/core';
import {MoneyApi} from "../../api/money-api";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {Util} from "../../util";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "@ngx-translate/core";
import {NgIf} from "@angular/common";
import {FileImportService} from "../../service/file-import-service";
import {AccountType} from "../../dto/account-type";
import {SecurityApi} from "../../api/security-api";
import {AuthService} from "../../service/auth.service";
import {DialogService} from "../../service/dialog.service";
import {MoneyImportResultComponent} from "./money-import-result/money-import-result.component";
import {SecurityImportResultComponent} from "./security-import-result/security-import-result.component";
import {MatButton} from "@angular/material/button";
import {SecurityTransactionImportLine} from "../../dto/security/security-transaction-import-line";
import {MoneyTransactionImportLine} from "../../dto/money/money-transaction-import-line";
import {SelectColumnsComponent} from "./select-columns/select-columns.component";
import {MatInput} from "@angular/material/input";
import {ImportFileProcessState} from "../../dto/import-file-process-state";
import {FileType} from "../../dto/file-type";
import {Observable, Observer} from "rxjs";
import {CsvService} from "../../service/csv.service";
import {ExcelService} from "../../service/excel.service";
import {TranslateService} from "../../service/translate.service";

@Component({
  selector: 'app-import',
  standalone: true,
  imports: [
    MatFormField,
    MatLabel,
    MatOption,
    MatSelect,
    ReactiveFormsModule,
    TranslatePipe,
    NgIf,
    MoneyImportResultComponent,
    SecurityImportResultComponent,
    MatButton,
    SelectColumnsComponent,
    MatInput
  ],
  providers: [TranslatePipe],
  templateUrl: './import.component.html',
  styleUrl: './import.component.scss'
})
export class ImportComponent implements OnInit {
  accounts: Account[];
  form: FormGroup;
  securityImportResult: SecurityTransactionImportLine[] | null;
  moneyImportResult: MoneyTransactionImportLine[] | null;
  importFileState: ImportFileProcessState;
  showCsvImportOptions: boolean;
  showCustomHeaderConfig: boolean;
  showLoadingText: boolean;

  @ViewChild(SelectColumnsComponent)
  selectColumnsComponent: SelectColumnsComponent;

  constructor(private accountApi: AccountApi,
              private fileImportService: FileImportService,
              private securityApi: SecurityApi,
              private moneyApi: MoneyApi,
              private authService: AuthService,
              private dialogService: DialogService,
              private csvService: CsvService,
              private excelService: ExcelService,
              private translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.accountApi.getAccounts().subscribe(accounts => {
        this.accounts = accounts;
      });

      let group: any = {
        account: new FormControl(''),
        csvSeparator: new FormControl(''),

      };
      this.form = new FormGroup(group);
      this.form.controls['csvSeparator'].setValue(';');
    });
  }

  onFileSelected(event: any) {
    this.securityImportResult = null;
    this.moneyImportResult = null;
    this.importFileState = new ImportFileProcessState();
    this.importFileState.file = event.target.files[0];
    this.showCsvImportOptions = false;
    this.showCustomHeaderConfig = false;
    this.processFileSelected();
  }

  processFileSelected() {
    if (this.importFileState.file) {
      this.fileImportService.setFileTypeAndContent(this.importFileState).subscribe(() => {
        if (this.importFileState.fileType == null) {
          this.dialogService.snackbar(this.translateService.translate('account/import/unknown-file-type'), this.translateService.translate('account/import/unknown-file-type'));
          return;
        }

        if (this.importFileState.fileType == FileType.CSV) {
          this.showCsvImportOptions = true;
        } else {
          this.processImportFileContent();
        }
      });
    }
  }

  processImportFileContent() {
    this.showCustomHeaderConfig = false;
    this.importFileState.accountType = null;
    if (this.importFileState.fileType != FileType.MT940) {
      this.parseImportFileContent().subscribe(() => {
        this.fileImportService.determineAccountTypeOnHeaders(this.importFileState);

        if (this.importFileState.accountType == null) {
          this.importFileState.accountType = this.form.value.account.accountType;
          this.showCustomHeaderConfig = true;
        } else if (this.importFileState.accountType != this.form.value.account.accountType) {
          this.dialogService.snackbar(this.translateService.translate('account/import/wrong-account-type'), this.translateService.translate('account/import/wrong-account-type'));
        } else {
          this.uploadFileToBackend(true);
        }
      });
    } else {
      this.importFileState.accountType = AccountType.DEBIT;
      this.uploadFileToBackend(true);

    }
  }

  parseImportFileContent(): Observable<void> {
    if (this.importFileState.fileType == FileType.CSV) {
      this.importFileState.csvSeparator = this.form.value.csvSeparator;
      return this.csvService.parseCsvFile(this.importFileState);
    } else if (this.importFileState.fileType == FileType.EXCEL) {
      return this.excelService.parseExcelFile(this.importFileState);
    } else {
      this.dialogService.snackbar(this.translateService.translate('account/import/unknown-file-type'), this.translateService.translate('account/import/unknown-file-type'));
      return new Observable((observer: Observer<void>) => {
      })
    }
  }

  uploadFileToBackend(dryRun: boolean) {
    this.securityImportResult = null;
    this.moneyImportResult = null;
    this.showLoadingText = true;

    let headers = this.selectColumnsComponent == null ? null : this.selectColumnsComponent.getHeaderConfig();
    let buyValue = this.selectColumnsComponent == null ? null : this.selectColumnsComponent.getBuyValue();
    let debitCreditValue = this.selectColumnsComponent == null ? null : this.selectColumnsComponent.getDebitCreditValue();
    let timestampFormat = this.selectColumnsComponent == null ? null : this.selectColumnsComponent.getTimestampFormatValue();
    let dateFormat = this.selectColumnsComponent == null ? null : this.selectColumnsComponent.getDateFormatValue();
    let timeFormat = this.selectColumnsComponent == null ? null : this.selectColumnsComponent.getTimeFormatValue();
    let csvSeparator = this.form.value.csvSeparator;

    if (this.importFileState.accountType == AccountType.SECURITY) {
      this.securityApi.importFile(this.form.value.account.id, this.importFileState.file, dryRun, headers, buyValue, timestampFormat, dateFormat, timeFormat, csvSeparator).subscribe(transactions => {
        this.securityImportResult = transactions;
        this.showLoadingText = false;
        this.dialogService.snackbar(this.translateService.translate('account/import/file-import-success'));
      });
    } else if (this.importFileState.accountType == AccountType.DEBIT) {
      this.moneyApi.importFile(this.form.value.account.id, this.importFileState.file, dryRun, headers, debitCreditValue, timestampFormat, dateFormat, timeFormat, csvSeparator).subscribe(transactions => {
        this.moneyImportResult = transactions;
        this.showLoadingText = false;
        this.dialogService.snackbar(this.translateService.translate('account/import/file-import-success'));
      });
    }
  }

  protected readonly Util = Util;
}
