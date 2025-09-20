import {Component, OnInit} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardModule} from "@angular/material/card";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {NgForOf, NgIf} from "@angular/common";
import {MatInput} from "@angular/material/input";
import {MatButton, MatFabButton} from "@angular/material/button";
import {Util} from "../../util";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {TranslatePipe} from "@ngx-translate/core";
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
    TranslatePipe,
    NgForOf,
    MoneyTransactionGroupQualifierListItemComponent,
    MatFabButton,
    MoneyTransactionGroupQualifierFormComponent
  ],
  templateUrl: './money-transaction-group-form.component.html',
  styleUrl: './money-transaction-group-form.component.scss'
})
export class MoneyTransactionGroupFormComponent implements OnInit {
  form: FormGroup;
  group: MoneyTransactionGroup;
  moneyTransactionGroupId: number;
  qualifierInEdit: MoneyTransactionGroupQualifier | null;
  nameInEdit: boolean;

  constructor(private api: MoneyApi,
              private router: Router,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.moneyTransactionGroupId = Util.getIdFromRoute(this.route, 'groupId');
      this.reset();
    });
  }

  reset() {
    let group: any = {
      name: new FormControl(''),
      dateRange: new FormControl(''),
      groupIds: new FormControl(''),
      amount: new FormControl(''),
    };
    this.form = new FormGroup(group);
    if (this.moneyTransactionGroupId != 0) {
      this.api.getMoneyTransactionGroup(this.moneyTransactionGroupId as number).subscribe(group => {
        this.group = group;
        this.form.controls['name'].setValue(group.name);
      });
    } else {
      this.group = new MoneyTransactionGroup();
    }
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateMoneyTransactionGroup(
      this.moneyTransactionGroupId,
      this.form.value.name,
      this.group == null ? [] : this.group.qualifiers
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
    if (qualifier != null && this.qualifierInEdit instanceof MoneyTransactionGroupQualifier) {
      // this.qualifierInEdit.qualifier = qualifier.qualifier;
      // this.qualifierInEdit.qualifierType = qualifier.qualifierType;
      // this.qualifierInEdit.transactionField = qualifier.transactionField;
    }
    this.onSaveButtonClicked();
    this.qualifierInEdit = null;
  }

  qualifierDeleteCallback() {

  }

  editQualifierCallback(qualifier: MoneyTransactionGroupQualifier) {
    this.qualifierInEdit = qualifier;
  }

  protected readonly Util = Util;
  protected readonly name = name;
}
