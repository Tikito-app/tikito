import {Component, OnInit} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardModule} from "@angular/material/card";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatFormField, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {NgForOf, NgIf} from "@angular/common";
import {MatInput} from "@angular/material/input";
import {MatButton, MatFabButton} from "@angular/material/button";
import {Util} from "../../util";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {TranslatePipe} from "../../service/translate-pipe.pipe";
import {ActivatedRoute, Router} from "@angular/router";
import {MoneyApi} from "../../api/money-api";
import MoneyTransactionGroupQualifier from "../../dto/money/money-transaction-group-qualifier";
import {
  MoneyTransactionGroupQualifierListItemComponent
} from "../money-transaction-group-qualifier-list-item/money-transaction-group-qualifier-list-item.component";
import {
  MoneyTransactionGroupQualifierFormComponent
} from "../money-transaction-group-qualifier-form/money-transaction-group-qualifier-form.component";
import {AuthService} from "../../service/auth.service";
import {MatOption, provideNativeDateAdapter} from "@angular/material/core";
import {MatSelect} from "@angular/material/select";
import {AccountApi} from "../../api/account-api";
import {Account} from "../../dto/account";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";

@Component({
  selector: 'app-moneyTransactionGroup-form',
  standalone: true,
  imports: [
    MatCardModule,
    MatCard,
    MatCardHeader,
    MatCardContent,
    ReactiveFormsModule,
    MatLabel,
    MatFormField,
    MatIcon,
    NgIf,
    MatInput,
    MatButton,
    NgForOf,
    MoneyTransactionGroupQualifierListItemComponent,
    MatFabButton,
    MoneyTransactionGroupQualifierFormComponent,
    MatOption,
    MatSelect,
    TranslatePipe,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatHint,
    MatSuffix
  ],
  templateUrl: './money-transaction-group-form.component.html',
  styleUrl: './money-transaction-group-form.component.scss',
  providers: [provideNativeDateAdapter()],
})
export class MoneyTransactionGroupFormComponent implements OnInit {
  form: FormGroup;
  group: MoneyTransactionGroup;
  moneyTransactionGroupId: number;
  qualifierInEdit: MoneyTransactionGroupQualifier | null;
  nameInEdit: boolean;
  accounts: Account[];

  constructor(private api: MoneyApi,
              private accountApi: AccountApi,
              private router: Router,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.moneyTransactionGroupId = Util.getIdFromRoute(this.route, 'groupId');
      this.accountApi.getAccounts().subscribe(accounts => {
        this.accounts = accounts;
        this.reset();
      });
    });
  }

  reset() {
    let group: any = {
      name: new FormControl(''),
      groupIds: new FormControl(''),
      accountIds: new FormControl(''),
      groupTypes: new FormControl(''),
      startDate: new FormControl(''),
      endDate: new FormControl(''),
      budgeted: new FormControl(''),
      dateRange: new FormControl(''),
      dateRangeAmount: new FormControl(''),
    };
    this.form = new FormGroup(group);
    if (this.moneyTransactionGroupId != 0) {
      this.api.getMoneyTransactionGroup(this.moneyTransactionGroupId as number).subscribe(group => {
        this.group = group;
        this.form.controls['name'].setValue(group.name);
        this.form.controls['groupTypes'].setValue(group.groupTypes);
        this.form.controls['accountIds'].setValue(group.accountIds);
        this.form.controls['startDate'].setValue(group.startDate);
        this.form.controls['endDate'].setValue(group.endDate);
        this.form.controls['budgeted'].setValue(group.budgeted);
        this.form.controls['dateRange'].setValue(group.dateRange);
        this.form.controls['dateRangeAmount'].setValue(group.dateRangeAmount);
      });
    } else {
      this.group = new MoneyTransactionGroup();
    }
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateMoneyTransactionGroup(
      this.moneyTransactionGroupId,
      this.form.value.name,
      this.form.value.groupTypes,
      this.group == null ? [] : this.group.qualifiers,
      this.form.value.accountIds,
      this.form.value.startDate,
      this.form.value.endDate,
      this.form.value.budgeted,
      this.form.value.dateRange,
      this.form.value.dateRangeAmount
    ).subscribe(group => {
      window.location.href = '/money/transaction-group/' + group.id;
    })
  }

  onCancelButtonClicked() {
    this.router.navigate(['/money/transaction-group']);
  }

  onDeleteButtonClicked() {
    if (this.moneyTransactionGroupId != null) {
      this.api.deleteMoneyTransactionGroup(this.moneyTransactionGroupId).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

  isQualifierInEdit(qualifier: MoneyTransactionGroupQualifier) {
    return this.qualifierInEdit != null &&
      this.qualifierInEdit.id == qualifier.id &&
      this.qualifierInEdit.transactionField == qualifier.transactionField &&
      this.qualifierInEdit.qualifier == qualifier.qualifier &&
      this.qualifierInEdit.qualifierType == qualifier.qualifierType;
  }

  onEditQualifierButtonClicked(qualifier: MoneyTransactionGroupQualifier) {
    this.qualifierInEdit = qualifier;
  }

  onAddQualifierButtonClicked() {
    let qualifier = new MoneyTransactionGroupQualifier()
    this.qualifierInEdit = qualifier;
    this.group.qualifiers.push(qualifier)
  }

  qualifierCallback(qualifier: MoneyTransactionGroupQualifier | null) {
    this.onSaveButtonClicked();
    this.qualifierInEdit = null;
  }

  qualifierDeleteCallback() {

  }

  editQualifierCallback(qualifier: MoneyTransactionGroupQualifier) {
    this.qualifierInEdit = qualifier;
  }

  protected readonly Util = Util;
}
