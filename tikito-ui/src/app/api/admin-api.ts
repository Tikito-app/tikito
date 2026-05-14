import {Observable} from "rxjs";
import {HttpService} from "../service/http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";
import {UserAccount} from "../dto/user-account";
import {Security} from "../dto/security/security";
import {SecurityType} from "../dto/security/security-type";
import {Account} from "../dto/account";
import {Isin} from "../dto/isin";
import {HttpRequestMethod} from "../dto/http-request-method";

@Injectable({
  providedIn: 'root'
})
export class AdminApi {

  constructor(private http: HttpService) {
  }

  getUsers(): Observable<UserAccount[]> {
    return this.http.httpGetList(UserAccount, new HttpRequestData()
      .withUrl('/api/admin/users'))
  }

  getSecurities(): Observable<Security[]> {
    return this.http.httpGetList(Security, new HttpRequestData()
      .withUrl('/api/admin/securities'));
  }

  getSecurity(securityId: number): Observable<Security> {
    return this.http.httpGetSingle(Security, new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId));
  }

  getIsins(securityId: number): Observable<Isin[]> {
    return this.http.httpGetList(Isin, new HttpRequestData()
      .withUrl('/api/security/' + securityId + '/isins'));
  }

  updateSecurity(securityId: number,
                 name: string,
                 securityType: SecurityType,
                 sector: string,
                 industry: string,
                 exchange: string,
                 currencyId: number) {
    return this.http.httpPost<Account>(new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId)
      .withBody({
        securityId: securityId,
        name: name,
        securityType: securityType,
        sector: sector,
        industry: industry,
        exchange: exchange,
      }));
  }

  deleteSecurity(securityId: number): Observable<void> {
    return this.http.httpDelete(new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId));
  }

  deleteIsin(isin: string): Observable<void> {
    return this.http.httpDelete(new HttpRequestData()
      .withUrl('/api/admin/securities/isin/' + isin));
  }

  updateIsin(isin: string, symbol: string, validFrom: any, validTo: any) {
    return this.http.httpPost<Security>(new HttpRequestData()
      .withUrl('/api/admin/securities/isin/' + isin)
      .withBody({
        symbol: symbol,
        validFrom: validFrom,
        validTo: validTo,
      }));
  }

  getIsin(isin: string): Observable<Isin> {
    return this.http.httpGetSingle(Isin, new HttpRequestData()
      .withUrl('/api/admin/securities/isin/' + isin));
  }

  getJobsCount(): Observable<number> {
    return this.http.basicHttpRequestWithErrorHandling(new HttpRequestData()
      .withRequestMethod(HttpRequestMethod.GET)
      .withUrl('/api/admin/jobs-count'));
  }

  recalculateHistoricalSecurityValue(securityId: number) {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId + '/recalculate-historical-value'));
  }

  updateSecurityPrices(securityId: number) {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId + '/update-prices'));
  }

  enrichSecurity(securityId: number) {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId + '/enrich'));
  }

  deleteSecurityPrices(securityId: number) {
    return this.http.httpDelete(new HttpRequestData()
      .withUrl('/api/admin/securities/' + securityId + '/delete-prices'));
  }

  updateAllSecurities() {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/securities/update-all'));
  }

  updateAllSecurityValues() {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/securities/update-all-values'));
  }

  recalculateHistoricalMoneyValue(accountId: number) {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/money/' + accountId + '/recalculate-historical-value'));
  }

  groupMoneyTransactions() {
    return this.http.httpPost(new HttpRequestData()
      .withUrl('/api/admin/money/group-transactions'));
  }

  recalculateHistoricalBudgetValue() {
    return this.http.basicHttpRequest(new HttpRequestData()
      .withRequestMethod(HttpRequestMethod.GET)
      .withUrl('/api/money/transactions-group/recalculate-historical-budget'))
  }
}
