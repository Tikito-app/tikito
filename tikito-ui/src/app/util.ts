import {Observable, Subject} from "rxjs";
import {BudgetDateRange} from "./dto/budget-date-range";
import {ActivatedRoute} from "@angular/router";
import {MoneyTransactionGroupQualifierType} from "./dto/money/money-transaction-group-qualifier-type";
import {MoneyTransactionField} from "./dto/money/money-transaction-field";
import {CurrencyPipe, DatePipe, PercentPipe} from "@angular/common";
import {AccountType} from "./dto/account-type";
import {SecurityHoldingGraphDisplayField} from "./dto/security/security-holding-graph-display-field";
import {SecurityType} from "./dto/security/security-type";
import {MoneyTransactionGroupType} from "./dto/money-transaction-group-type";
import {LoanType} from "./dto/loan-type";
import {CacheService} from "./service/cache-service";

export class Util {
  public static DATE_FORMAT: string = 'dd-MM-YYYY';
  public static TIMESTAMP_FORMAT: 'dd-LL-YYYY HH:mm';
  public static PAGE_SIZE_OPTIONS = [5, 10, 20];
  public static DEFAULT_PAGE_SIZE = 10;

  public static COLORS: string[] = [
    '#3494ff',  // Blue
    '#ff85bc',  // Pink
    '#30c36d',  // Green
    '#37c3d0',  // Teal
    '#ffae25',  // Yellow
    '#fc5b4b',  // Red
    '#9b4d96',  // Purple
    '#f8c8d2',  // Light Pink
    '#ff6347',  // Tomato Red
    '#7bed9f',  // Pastel Green
    '#ff9f43',  // Orange
    '#ff577f',  // Soft Red
    '#6a4c93',  // Lavender Purple
    '#f1f2f6',  // Light Gray (neutral)
    '#8e44ad',  // Purple
    '#fd79a8',  // Light Pink
    '#2c3e50',  // Deep Blue
    '#e74c3c',  // Vibrant Red
    '#1abc9c',  // Turquoise
    '#f39c12'   // Golden Yellow
  ];

  public static jwtToObject(jwt: string) {
    return JSON.parse(atob(jwt.split('.')[1]));
  }

  public static isJwtValid(jwtString: string | null) {
    if (jwtString == null || jwtString == '') {
      return false;
    }
    let jwt = this.jwtToObject(jwtString);
    let currentEpoch = Math.floor(new Date().getTime() / 1000);
    return jwt.exp > currentEpoch;
  }

  public static waitAll(args: Observable<any>[]): Observable<any[]> {
    const final = new Subject<any[]>();
    const flags = new Array(args.length);
    const result = new Array(args.length);
    let total = args.length;
    for (let i = 0; i < args.length; i++) {
      flags[i] = false;
      args[i].subscribe(
        res => {
          if (flags[i] === false) {
            flags[i] = true;
            result[i] = res;
            total--;
            if (total < 1) {
              final.next(result);
            }
          }
        },
        error => {
          if (flags[i] === false) {
            flags[i] = true;
            result[i] = error;
            total--;
            if (total < 1) {
              final.next(result);
            }
          }
        }
      );
    }
    return final.asObservable();
  }

  static hasText(text: string | null | undefined) {
    return text != null && text.trim().length > 0;
  }

  static toEnum<T>(type: any, value: string, defaultValue: string): T {
    let keys = Object.keys(type);
    if (keys.includes(value)) {
      return value as T;
    }
    return defaultValue as T;
  }

  static toBool(value: any, defaultValue: boolean): boolean {
    if (value == null || value == '0' || value == 'false') {
      return false;
    } else if (value != null && value == '1' || value == 'true') {
      return true;
    }
    return defaultValue;
  }

  static toNumber(value: any, defaultValue: number): number {
    try {
      return parseInt(value);
    } catch (e) {
      return defaultValue;
    }
  }

  static getBudgetDateRanges(): string[] {
    return Object
      .keys(BudgetDateRange)
      .filter((v) => isNaN(Number(v)))
  }

  static getQualifierTypes(): string[] {
    return Object
      .keys(MoneyTransactionGroupQualifierType)
      .filter((v) => isNaN(Number(v)));
  }

  static getMoneyTransactionFields(): string[] {
    return Object
      .keys(MoneyTransactionField)
      .filter((v) => isNaN(Number(v)));
  }

  static getAccountTypes(): string[] {
    return Object
      .keys(AccountType)
      .filter((v) => isNaN(Number(v)));
  }

  static getSecurityTypes(): string[] {
    return Object
      .keys(SecurityType)
      .filter((v) => isNaN(Number(v)));
  }

  static getSecurityHoldingGraphDisplayFields(): string[] {
    return Object
      .keys(SecurityHoldingGraphDisplayField)
      .filter((v) => isNaN(Number(v)));
  }

  static getMoneyTransactionGroupTypes(): string[] {
    return Object
      .keys(MoneyTransactionGroupType)
      .filter((v) => isNaN(Number(v)));
  }

  static getLoanTypes(): string[] {
    return Object
      .keys(LoanType)
      .filter((v) => isNaN(Number(v)));
  }

  static getIdFromRoute(route: ActivatedRoute, key: string): number {
    let value = route.snapshot.paramMap.get(key) as string;
    if (value == null) {
      return 0;
    }
    try {
      let n = parseInt(value);
      if(Number.isNaN(n)) {
        return 0;
      }
      return n;
    } catch (e) {
      return 0;
    }
  }

  static getFromRoute(route: ActivatedRoute, key: string): string | null {
    return route.snapshot.paramMap.get(key);
  }

  static getUrlFragment(key: string): string | null {
    let fragment = window.location.hash;
    if (fragment == null || fragment == '') {
      return null;
    }
    fragment = fragment.substring(1);
    let parts = fragment.split(';').map(part => part.split('=')).filter(part => part.length == 2 && part[0] == key);
    if (parts.length > 0) {
      return parts[0][1];
    }
    return null;
  }

  static addDays(date: Date, days: number): Date {
    let newDate = new Date(date);
    newDate.setDate(date.getDate() + days);
    return newDate;
  }

  static formatDate(date: Date, format: string): string {
    const datepipe: DatePipe = new DatePipe('en-US')
    return datepipe.transform(date, format) as string;
  }

  static currencyFormat(value: number): string {
    return this.currencyFormatWithSymbol(value, 47);
  }

  static currencyFormatWithSymbol(value: number, currencyId: number): string {
    let currency = CacheService.getCurrencyById(currencyId);
    return new CurrencyPipe('en-EN', currency == null ? 'EUR' : currency.currentIsin).transform(value) as string;
  }

  static percentageFormat(value: number): string {
    return new PercentPipe('en-EN').transform(value / 100) as string
  }

  static currencyColor(value: number): string {
    if (value < 0) {
      return 'red';
    } else if (value > 0) {
      return 'green';
    }
    return '';
  }

  static getColor(colorIndex: number) {
    return this.COLORS[(colorIndex) % this.COLORS.length];
  }

  static toLocalDate(date: any) :string {
    if(date == null || date == '') {
      return null as unknown as string;
    } else if(typeof(date) == 'string') {
      return date;
    }
    return date.getFullYear() + '-' + Util.zeroPad(date.getMonth() + 1) + '-' + Util.zeroPad(date.getDate());
  }

  static zeroPad(num: number): string {
    if(num < 10) {
      return '0' + num;
    }
    return '' + num;
  }
}
