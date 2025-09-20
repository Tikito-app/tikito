import {Observable} from "rxjs";
import {HttpService} from "../service/http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";
import {Account} from "../dto/account";
import {AccountType} from "../dto/account-type";
import {HttpRequestMethod} from "../dto/http-request-method";

@Injectable({
  providedIn: 'root'
})
export class AccountApi {

  constructor(private http: HttpService) {
  }

  getAccounts(): Observable<Account[]> {
    return this.http.httpGetList<Account>(Account, new HttpRequestData()
      .withUrl('/api/account'))
  }

  getAccount(id: number): Observable<Account> {
    return this.http.httpGetSingle<Account>(Account, new HttpRequestData()
      .withUrl('/api/account/' + id))
  }

  createOrUpdateAccount(accountId: number | null, name: string, accountNumber: string, accountType: AccountType, currencyId: number) {
    return this.http.httpPost<Account>(new HttpRequestData()
      .withUrl('/api/account')
      .withBody({
        id: accountId,
        name: name,
        accountNumber: accountNumber,
        accountType: accountType,
        currencyId: currencyId,
      }));
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.httpDelete(new HttpRequestData().withUrl('/api/account/' + id))
  }

  getImporterTypesHeaders(): Observable<any> {
    return this.http.basicHttpRequest(new HttpRequestData()
      .withRequestMethod(HttpRequestMethod.GET)
      .withUrl('/api/account/importer-types-headers'));
  }
}
