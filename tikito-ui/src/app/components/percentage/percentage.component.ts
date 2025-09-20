import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-percentage',
  standalone: true,
  imports: [],
  templateUrl: './percentage.component.html',
  styleUrl: './percentage.component.scss'
})
export class PercentageComponent {
  @Input()
  amount: number;

  getStyle(): string {
    if (this.amount < 0) {
      return 'color: red';
    } else if (this.amount > 0) {
      return 'color: green';
    }
    return '';
  }

  protected readonly Math = Math;
}
