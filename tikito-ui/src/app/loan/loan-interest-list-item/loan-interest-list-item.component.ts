import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslatePipe} from "@ngx-translate/core";
import {LoanInterest} from "../../dto/loan-interest";

@Component({
  selector: 'app-loan-interest-list-item',
  standalone: true,
  imports: [],
  providers: [TranslatePipe],
  templateUrl: './loan-interest-list-item.component.html',
  styleUrl: './loan-interest-list-item.component.scss'
})
export class LoanInterestListItemComponent {

  @Input()
  interest: LoanInterest;

  @Output()
  onEditCallback: EventEmitter<void> = new EventEmitter();
}
