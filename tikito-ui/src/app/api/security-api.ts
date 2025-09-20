import {Observable} from "rxjs";
import {HttpService} from "../service/http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";
import SecurityHolding from "../dto/security/security-holding";
import {SecurityTransaction} from "../dto/security/security-transaction";
import HistoricalHoldingValue from "../dto/security/historical-holding-value";
import {SecurityHoldingFilter} from "../dto/security/security-holding-filter";
import AggregatedHistoricalHoldingsValue from "../dto/security/aggregated-historical-holdings-value";
import {Security} from "../dto/security/security";
import {SecurityType} from "../dto/security/security-type";
import {SecurityTransactionImportLine} from "../dto/security/security-transaction-import-line";

@Injectable({
  providedIn: 'root'
})
export class SecurityApi {

  constructor(private http: HttpService) {
  }

  getSecurities(type: SecurityType): Observable<Security[]> {
    return this.http.httpGetList<Security>(Security, new HttpRequestData()
      .withUrl('/api/security' + (type == null ? '' : '?type=' + type)));
  }

  getSecurityHoldings(): Observable<SecurityHolding[]> {
    return this.http.httpGetList<SecurityHolding>(SecurityHolding, new HttpRequestData()
      .withUrl('/api/security/holding'));
  }

  getSecurityHolding(holdingId: number): Observable<SecurityHolding> {
    return this.http.httpGetSingle<SecurityHolding>(SecurityHolding, new HttpRequestData()
      .withUrl('/api/security/holding/' + holdingId + '/details'));
  }

  getTransactions(filter: SecurityHoldingFilter): Observable<SecurityTransaction[]> {
    return this.http.httpPostList<SecurityTransaction>(SecurityTransaction, new HttpRequestData()
      .withUrl('/api/security/holding/transactions')
      .withBody(filter));
  }

  getHistoricalValues(filter: SecurityHoldingFilter): Observable<HistoricalHoldingValue[]> {
    return this.http.httpPostList<HistoricalHoldingValue>(HistoricalHoldingValue, new HttpRequestData()
      .withUrl('/api/security/holding/historical-values')
      .withBody(filter));
  }

  getAggregatedHistoricalValues(): Observable<AggregatedHistoricalHoldingsValue[]> {
    return this.http.httpGetList<AggregatedHistoricalHoldingsValue>(AggregatedHistoricalHoldingsValue, new HttpRequestData()
      .withUrl('/api/security/holding/aggregated-historical-values'));
  }

  importFile(accountId: number, file: File, dryRun: boolean, customHeaderConfig: any, buyValue: string, timestampFormat: string, dateFormat: string, timeFormat: string, csvSeparator: string): Observable<SecurityTransactionImportLine[]> {
    const formData = new FormData();
    formData.append("header-config", JSON.stringify(customHeaderConfig));
    formData.append("file", file);
    formData.append("csv-separator", csvSeparator);
    formData.append("timestamp-format", timestampFormat);
    formData.append("time-format", timeFormat);
    formData.append("date-format", dateFormat);
    formData.append("buy-value", buyValue);
    formData.append("dryRun", dryRun ? 'true' : 'false');

    return this.http.httpPost<SecurityTransactionImportLine[]>(new HttpRequestData()
      .withUrl('/api/security/transaction/' + accountId + '/import')
      .withBody(formData));
  }

  deleteSecurityHolding(securityHoldingId: number) {
    return this.http.httpDelete(new HttpRequestData()
      .withUrl('/api/security/holding/' + securityHoldingId));
  }

  deleteSecurityTransaction(transactionId: number) {
    return this.http.httpDelete(new HttpRequestData()
      .withUrl('/api/security/transaction/' + transactionId));
  }
}
