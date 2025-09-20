import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import MoneyTransactionGroupQualifier from "../../dto/money/money-transaction-group-qualifier";
import {BudgetApi} from "../../api/budget-api";
import {ActivatedRoute, Router} from "@angular/router";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatOption, MatSelect} from "@angular/material/select";
import {TranslatePipe} from "@ngx-translate/core";
import {Util} from "../../util";
import {MatCard, MatCardContent, MatCardHeader, MatCardTitle} from "@angular/material/card";
import {MatIcon} from "@angular/material/icon";
import {NgIf} from "@angular/common";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {AuthService} from "../../service/auth.service";

@Component({
  selector: 'app-money-transaction-group-qualifier-form',
  standalone: true,
  imports: [
    MatFormField,
    MatSelect,
    TranslatePipe,
    MatOption,
    MatCard,
    MatCardHeader,
    MatCardContent,
    MatIcon,
    ReactiveFormsModule,
    MatLabel,
    MatCardTitle,
    NgIf,
    MatInput,
    MatButton
  ],
  templateUrl: './money-transaction-group-qualifier-form.component.html',
  styleUrl: './money-transaction-group-qualifier-form.component.scss'
})
export class MoneyTransactionGroupQualifierFormComponent implements OnInit {
  form: FormGroup;

  @Input()
  qualifier: MoneyTransactionGroupQualifier;

  @Output()
  callback: EventEmitter<MoneyTransactionGroupQualifier | null> = new EventEmitter();

  @Output()
  deleteCallback: EventEmitter<void> = new EventEmitter();

  constructor(private api: BudgetApi,
              private router: Router,
              private authService: AuthService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.authService.onSystemReady((loggedInUser: any) => {
      this.reset();
    });
  }

  reset() {
    let formGroup: any = {
      qualifierType: new FormControl(''),
      transactionField: new FormControl(''),
      qualifier: new FormControl(''),
    };
    this.form = new FormGroup(formGroup);

    this.form.controls['qualifierType'].setValue(this.qualifier.qualifierType);
    this.form.controls['transactionField'].setValue(this.qualifier.transactionField);
    this.form.controls['qualifier'].setValue(this.qualifier.qualifier);
  }

  onSaveButtonClicked() {
    this.qualifier.qualifierType = this.form.value['qualifierType'];
    this.qualifier.qualifier = this.form.value['qualifier'];
    this.qualifier.transactionField = this.form.value['transactionField'];
    this.callback.next(new MoneyTransactionGroupQualifier());
  }

  onCancelButtonClicked() {
    this.callback.next(null);
  }

  onDeleteButtonClicked() {
    this.deleteCallback.next();
  }

  protected readonly Util = Util;
}
