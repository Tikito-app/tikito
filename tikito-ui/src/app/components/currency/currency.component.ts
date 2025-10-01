import {Component, Input} from '@angular/core';
import {CurrencyPipe, NgIf} from "@angular/common";
import {CacheService} from "../../service/cache-service";

@Component({
  selector: 'app-currency',
  standalone: true,
  imports: [
    CurrencyPipe,
    NgIf
  ],
  templateUrl: './currency.component.html',
  styleUrl: './currency.component.scss'
})
export class CurrencyComponent {
  @Input()
  amount: number;

  @Input()
  currencyId: number | null;

  getStyle(): string {
    if(this.amount < 0) {
      return 'color: red';
    } else if(this.amount > 0) {
      return 'color: green';
    }
    return '';
  }

  getCurrencySymbol(): string {
    let currency = CacheService.getCurrencyById(this.currencyId as number);
    if(currency == null) {
      return null as unknown as string;
    }
    return currency.currentIsin;
  }

  protected readonly CacheService = CacheService;
}
