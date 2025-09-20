import {AfterViewInit, Component} from '@angular/core';
import {MatCard, MatCardContent, MatCardHeader, MatCardModule} from "@angular/material/card";
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatIcon} from "@angular/material/icon";
import {NgIf} from "@angular/common";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {Util} from "../../util";
import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import {TranslatePipe} from "@ngx-translate/core";
import {BudgetApi} from "../../api/budget-api";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-budget-form',
  standalone: true,
  imports: [
    MatCardModule,
    MatCard,
    MatCardHeader,
    MatCardContent,
    ReactiveFormsModule,
    MatLabel,
    MatFormField,
    MatSelect,
    MatIcon,
    NgIf,
    MatInput,
    MatOption,
    MatButton,
    TranslatePipe
  ],
  templateUrl: './budget-form.component.html',
  styleUrl: './budget-form.component.scss'
})
export class BudgetFormComponent implements AfterViewInit {
  form: FormGroup;
  groups: MoneyTransactionGroup[];
  budgetId: number | null;

  constructor(private api: BudgetApi,
              private router: Router,
              private route: ActivatedRoute) {
  }

  ngAfterViewInit() {
    this.budgetId = Util.getIdFromRoute(this.route, 'budgetId');

    this.reset();
  }

  reset() {
    this.api.getAvailableTransactionGroups(this.budgetId).subscribe(groups => {
      this.groups = groups;

      let group: any = {
        name: new FormControl(''),
        dateRange: new FormControl(''),
        groupIds: new FormControl(''),
        amount: new FormControl(''),
      };
      this.form = new FormGroup(group);

      if (this.budgetId != null) {
        this.api.getBudget(this.budgetId).subscribe(budget => {
          this.form.controls['name'].setValue(budget.name);
          this.form.controls['amount'].setValue(budget.amount);
          this.form.controls['dateRange'].setValue(budget.dateRange);
          this.form.controls['groupIds'].setValue(budget.groups.map(group => group.id));
        });
      }
    });
  }

  onSaveButtonClicked() {
    this.api.createOrUpdateBudget(
      this.budgetId,
      this.form.value.name,
      this.form.value.amount,
      this.form.value.dateRange,
      this.form.value.groupIds).subscribe(budget => {
        this.router.navigate(['/budget']);
    })
  }

  onCancelButtonClicked() {
    this.router.navigate(['/budget']);
  }

  onDeleteButtonClicked() {
    if(this.budgetId != null) {
      this.api.deleteBudget(this.budgetId).subscribe(() => this.onCancelButtonClicked());
    } else {
      this.onCancelButtonClicked();
    }
  }

  protected readonly Util = Util;
}
