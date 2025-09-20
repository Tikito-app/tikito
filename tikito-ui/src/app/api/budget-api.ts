import {HttpService} from "../service/http.service";
import {Observable} from "rxjs";
import MoneyTransactionGroup from "../dto/money/money-transaction-group";
import {HttpRequestData} from "../dto/http-request-data";
import {BudgetDateRange} from "../dto/budget-date-range";
import Budget from "../dto/budget";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class BudgetApi {
  constructor(private http: HttpService) {
  }

  getBudgets(): Observable<Budget[]> {
    return this.http.httpGetList<Budget>(Budget, new HttpRequestData().withUrl('/api/budget'));
  }

  getAvailableTransactionGroups(budgetId: number | null): Observable<MoneyTransactionGroup[]> {
    return this.http.httpGetList<MoneyTransactionGroup>(MoneyTransactionGroup, new HttpRequestData()
      .withUrl('/api/budget/available-transaction-groups' + (budgetId != null ? '/' + budgetId : '')))
  }

  createOrUpdateBudget(id: number | null,
                       name: string,
                       amount: number,
                       dateRange: BudgetDateRange,
                       groupIds: number[]): Observable<Budget> {
    return this.http.httpPost<Budget>(new HttpRequestData()
      .withUrl('/api/budget')
      .withBody({
        id: id != null ? id : null,
        name: name,
        amount: amount,
        dateRange: dateRange,
        groupIds: groupIds
      }))
  }

  getBudget(budgetId: number): Observable<Budget> {
    return this.http.httpGetSingle<Budget>(Budget, new HttpRequestData().withUrl('/api/budget/' + budgetId));
  }

  deleteBudget(budgetId: number): Observable<void> {
    return this.http.httpDelete(new HttpRequestData().withUrl('/api/budget/' + budgetId));
  }
}
