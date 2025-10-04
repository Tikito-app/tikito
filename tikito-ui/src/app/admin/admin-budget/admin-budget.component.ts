import { Component } from '@angular/core';
import {MatButton} from "@angular/material/button";
import {TranslatePipe} from "@ngx-translate/core";
import {BudgetApi} from "../../api/budget-api";

@Component({
  selector: 'app-admin-budget',
  standalone: true,
    imports: [
        MatButton,
        TranslatePipe
    ],
  templateUrl: './admin-budget.component.html',
  styleUrl: './admin-budget.component.scss'
})
export class AdminBudgetComponent {

    constructor(private api: BudgetApi) {
    }

    updateAll() {
        this.api.updateAll();
    }
}
