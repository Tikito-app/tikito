import {Component, EventEmitter, Input, Output} from '@angular/core';
import MoneyTransactionGroupQualifier from "../../dto/money/money-transaction-group-qualifier";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
  selector: 'app-money-transaction-group-qualifier-list-item',
  standalone: true,
  imports: [
    TranslatePipe
  ],
  templateUrl: './money-transaction-group-qualifier-list-item.component.html',
  styleUrl: './money-transaction-group-qualifier-list-item.component.scss'
})
export class MoneyTransactionGroupQualifierListItemComponent {
  @Input()
  qualifier: MoneyTransactionGroupQualifier

  @Output()
  onEditCallback: EventEmitter<void> = new EventEmitter();
}
