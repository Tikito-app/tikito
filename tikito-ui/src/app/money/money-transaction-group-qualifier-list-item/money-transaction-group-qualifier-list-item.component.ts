import {Component, EventEmitter, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import MoneyTransactionGroupQualifier from "../../dto/money/money-transaction-group-qualifier";
import {TranslatePipe} from "@ngx-translate/core";

@Component({
    selector: 'app-money-transaction-group-qualifier-list-item',
    imports: [
        TranslatePipe
    ],
    templateUrl: './money-transaction-group-qualifier-list-item.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './money-transaction-group-qualifier-list-item.component.scss'
})
export class MoneyTransactionGroupQualifierListItemComponent {
  @Input()
  qualifier: MoneyTransactionGroupQualifier

  @Output()
  onEditCallback: EventEmitter<void> = new EventEmitter();
}
