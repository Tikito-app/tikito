import {Injectable, OnInit} from "@angular/core";
import {Account} from "../dto/account";
import {AccountApi} from "../api/account-api";
import {SecurityApi} from "../api/security-api";
import {Security} from "../dto/security/security";
import {SecurityType} from "../dto/security/security-type";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class CacheService implements OnInit {
  static accounts: Account[] = [];
  static currencies: Security[] = [];
  static currenciesByIsin: any = [];
  static currenciesById: any = [];

  static accountApi: AccountApi;
  static securityApi: SecurityApi;

  constructor(private accountApi: AccountApi,
              private securityApi: SecurityApi) {
    CacheService.accountApi = accountApi;
    CacheService.securityApi = securityApi;
  }

  static init(): Observable<void> {
    return new Observable(observer => {
      CacheService.securityApi.getSecurities(SecurityType.CURRENCY).subscribe(currencies => {
        CacheService.currenciesByIsin = [];
        CacheService.currenciesById = [];

        currencies.forEach(currency => {
          CacheService.currenciesByIsin[currency.currentIsin] = currency;
          CacheService.currenciesById[currency.id] = currency;
        });

        CacheService.accountApi.getAccounts().subscribe(accounts => {
          accounts.forEach(account => CacheService.accounts[account.id] = account);
          CacheService.currencies = currencies
            .sort((a, b) => {
              let isinA = a.currentIsin;
              let isinB = b.currentIsin;

              if (isinB == 'EUR') {
                return 1;
              } else if(isinB == 'USD' && isinA != 'EUR') {
                return 1;
              }
              return isinA.localeCompare(b.currentIsin);
            });
          observer.next();
        });
      });
    });
  }

  ngOnInit(): void {
  }

  static getAccountById(accountId: number): Account {
    if (CacheService.accounts[accountId] == null) {
      let account = new Account();
      account.name = 'unknown';
      return account;
    }
    return CacheService.accounts[accountId];
  }

  static getCurrencyByIsin(isin: string): Security {
    return CacheService.currenciesByIsin[isin];
  }

  static getCurrencies(): Security[] {
    return CacheService.currencies;
  }

  static getCurrencyById(id: number): Security {
    return CacheService.currenciesById[id];
  }
}
