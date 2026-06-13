import {Component, Input} from '@angular/core';
import {CurrencyPipe} from "@angular/common";
import {CacheService} from "../../service/cache-service";
import {SecurityType} from "../../dto/security/security-type";
import {Security} from "../../dto/security/security";

@Component({
    selector: 'app-currency',
    imports: [
        CurrencyPipe,
    ],
    templateUrl: './currency.component.html',
    styleUrl: './currency.component.scss'
})
export class CurrencyComponent {
  @Input()
  amount: number;

  @Input()
  currencyId: number | null;

  currency: Security | null;

  getStyle(): string {
    if (this.amount < 0) {
      return 'color: red';
    } else if (this.amount > 0) {
      return 'color: green';
    }
    return '';
  }

  assertCurrency(): boolean {
    if (this.currency != null) {
      return true;
    }
    this.currency = CacheService.getCurrencyById(this.currencyId as number);
    return this.currency != null;

  }

  getCurrencySymbol(): string {
    if (!this.assertCurrency()) {
      return null as unknown as string;
    }
    return (this.currency as Security).currentIsin;
  }

  isCrypto(): boolean {
    if (!this.assertCurrency()) {
      return false;
    }
    return (this.currency as Security).securityType == SecurityType.CRYPTO;
  }

  protected readonly CacheService = CacheService;
}
