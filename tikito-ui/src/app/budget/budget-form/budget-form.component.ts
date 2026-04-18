import {AfterViewInit, Component, EventEmitter, Input, Output} from '@angular/core';
import {MatCardModule} from "@angular/material/card";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatFormField, MatHint, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatIcon} from "@angular/material/icon";
import {NgIf} from "@angular/common";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {Util} from "../../util";
import {TranslatePipe} from "../../service/translate-pipe.pipe";
import {BudgetApi} from "../../api/budget-api";
import {ActivatedRoute, Router} from "@angular/router";
import {MoneyApi} from "../../api/money-api";
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {provideNativeDateAdapter} from "@angular/material/core";
import Budget from "../../dto/budget/budget";
import {LoanInterest} from "../../dto/loan-interest";

@Component({
  selector: 'app-budget-form',
  standalone: true,
  imports: [
    MatCardModule,
    ReactiveFormsModule,
    MatLabel,
    MatFormField,
    MatSelect,
    MatIcon,
    NgIf,
    MatInput,
    MatOption,
    MatButton,
    TranslatePipe,
    MatDatepicker,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatHint,
    MatSuffix
  ],
  templateUrl: './budget-form.component.html',
  styleUrl: './budget-form.component.scss',
  providers: [provideNativeDateAdapter()],
})
export class BudgetFormComponent implements AfterViewInit {
  form: FormGroup;

  @Input()
  budget: Budget;

  @Output()
  callback: EventEmitter<Budget | null> = new EventEmitter();

  constructor(private api: BudgetApi,
              private router: Router,
              private moneyApi: MoneyApi,
              private route: ActivatedRoute) {
  }

  ngAfterViewInit() {
    this.reset();
  }

  reset() {
    this.moneyApi.getMoneyTransactionGroups().subscribe(groups => {
      let group: any = {
        startDate: new FormControl(''),
        dateRange: new FormControl(''),
        dateRangeAmount: new FormControl(''),
        amount: new FormControl(''),
      };
      this.form = new FormGroup(group);

      if (this.budget != null) {
        this.form.controls['amount'].setValue(this.budget.amount);
        this.form.controls['startDate'].setValue(this.budget.startDate);
        this.form.controls['dateRange'].setValue(this.budget.dateRange);
        this.form.controls['dateRangeAmount'].setValue(this.budget.dateRangeAmount);
      }
    });
  }

  onSaveButtonClicked() {
    this.budget.amount = this.form.value.amount;
    this.budget.startDate = this.form.value.startDate;
    this.budget.endDate = this.form.value.endDate;
    this.budget.amount = this.form.value.dateRange;
    this.budget.dateRangeAmount = this.form.value.dateRangeAmount;
    this.budget.dateRange = this.form.value.dateRange;
    this.callback.next(this.budget);
  }

  onCancelButtonClicked() {
    this.callback.next(null);
  }

  onDeleteButtonClicked() {
    if (this.budget != null) {
      this.api.deleteBudget(this.budget.id).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

  protected readonly Util = Util;
}
