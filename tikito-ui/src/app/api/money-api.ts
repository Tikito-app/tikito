import {Observable} from "rxjs";
import MoneyTransactionGroup from "../dto/money/money-transaction-group";
import {HttpService} from "../service/http.service";
import {HttpRequestData} from "../dto/http-request-data";
import {Injectable} from "@angular/core";
import MoneyTransaction from "../dto/money/money-transaction";
import MoneyTransactionGroupQualifier from "../dto/money/money-transaction-group-qualifier";
import {MoneyTransactionsFilter} from "../dto/money/money-transactions-filter";
import {AggregatedHistoricalMoneyHoldingValue} from "../dto/money/aggregated-historical-money-holding-value";
import {MoneyTransactionImportLine} from "../dto/money/money-transaction-import-line";
import {HttpRequestMethod} from "../dto/http-request-method";

@Injectable({
  providedIn: 'root'
})
export class MoneyApi {

  constructor(private http: HttpService) {
  }

  getMoneyTransactionGroups(): Observable<MoneyTransactionGroup[]> {
    return this.http.httpGetList<MoneyTransactionGroup>(MoneyTransactionGroup, new HttpRequestData()
      .withUrl('/api/money/transactions-group'));
  }

  getMoneyTransactionGroup(groupId: number): Observable<MoneyTransactionGroup> {
    return this.http.httpGetSingle<MoneyTransactionGroup>(MoneyTransactionGroup, new HttpRequestData()
      .withUrl('/api/money/transactions-group/' + groupId + '/details'));
  }

  getAggregatedHistoricalMoneyHoldingValues(): Observable<AggregatedHistoricalMoneyHoldingValue[]> {
    return this.http.httpGetList<AggregatedHistoricalMoneyHoldingValue>(AggregatedHistoricalMoneyHoldingValue, new HttpRequestData()
      .withUrl('/api/money/holding/aggregated-historical-values'));
  }

  getTransactions(filter: MoneyTransactionsFilter): Observable<MoneyTransaction[]> {
    return this.http.httpPostList<MoneyTransaction>(MoneyTransaction, new HttpRequestData()
      .withUrl('/api/money/transaction')
      .withBody(filter));
  }

  createOrUpdateMoneyTransactionGroup(id: number | null, name: string, qualifiers: MoneyTransactionGroupQualifier[]): Observable<MoneyTransactionGroup> {
    return this.http.httpPost<MoneyTransactionGroup>(new HttpRequestData()
      .withUrl('/api/money/transactions-group')
      .withBody({
        id: id,
        name: name,
        qualifiers: qualifiers
      }));
  }

  deleteMoneyTransactionGroup(id: number) {
    return this.http.httpDelete(new HttpRequestData().withUrl('/api/money/transactions-group/' + id))
  }

  importFile(accountId: number, file: File, dryRun: boolean, customHeaderConfig: any, debitCreditValue: string, timestampFormat: string, dateFormat: string, timeFormat: string, csvSeparator: string): Observable<MoneyTransactionImportLine[]> {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("csv-separator", csvSeparator);
    formData.append("timestamp-format", timestampFormat);
    formData.append("time-format", timeFormat);
    formData.append("date-format", dateFormat);
    formData.append("debit-credit-value", debitCreditValue);
    formData.append("header-config", JSON.stringify(customHeaderConfig));
    formData.append('dryRun', dryRun ? 'true' : 'false')

    return this.http.httpPost<MoneyTransactionImportLine[]>(new HttpRequestData()
      .withUrl('/api/money/transaction/' + accountId + '/import')
      .withBody(formData));
  }

  deleteMoneyTransaction(transactionId: number) {
    return this.http.httpDelete(new HttpRequestData()
      .withUrl('/api/money/transaction/' + transactionId));
  }

  setTransactionGroup(transactionId: number, groupId: number | null): Observable<MoneyTransaction> {
    let body: any = {
      transactionId: transactionId
    };

    if(groupId != null) {
      body['groupId'] = groupId;
    }

    return this.http.httpPostSingle(MoneyTransaction, new HttpRequestData()
      .withUrl('/api/money/transaction/set-group-id')
      .withBody(body));
  }
}
