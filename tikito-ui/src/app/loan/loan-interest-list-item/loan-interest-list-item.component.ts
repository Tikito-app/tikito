import {Component, EventEmitter, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import {LoanInterest} from "../../dto/loan-interest";

@Component({
    selector: 'app-loan-interest-list-item',
    imports: [],
    templateUrl: './loan-interest-list-item.component.html',
    changeDetection: ChangeDetectionStrategy.Eager,
    styleUrl: './loan-interest-list-item.component.scss'
})
export class LoanInterestListItemComponent {

  @Input()
  interest: LoanInterest;

  @Output()
  onEditCallback: EventEmitter<void> = new EventEmitter();
}
