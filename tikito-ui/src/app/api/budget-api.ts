import {HttpService} from "../service/http.service";
import {Observable} from "rxjs";
import {HttpRequestData} from "../dto/http-request-data";
import {BudgetDateRange} from "../dto/budget/budget-date-range";
import Budget from "../dto/budget/budget";
import {Injectable} from "@angular/core";
import {HistoricalBudgetValue} from "../dto/budget/historical-budget-value";
import {HttpRequestMethod} from "../dto/http-request-method";
import {MoneyTransactionsFilter} from "../dto/money/money-transactions-filter";
import moment from "moment/moment";

@Injectable({
    providedIn: 'root'
})
export class BudgetApi {
    constructor(private http: HttpService) {
    }

    getBudgets(): Observable<Budget[]> {
        return this.http.httpGetList<Budget>(Budget, new HttpRequestData().withUrl('/api/budget'));
    }

    getBudgetsByFilter(filter: MoneyTransactionsFilter): Observable<Budget[]> {
        return this.http.httpPostList<Budget>(Budget, new HttpRequestData()
          .withBody(filter)
          .withUrl('/api/budget/filter'));
    }

    createOrUpdateBudget(groupId: number,
                         amount: number,
                         startDate: string,
                         endDate: string,
                         dateRange: BudgetDateRange,
                         dateRangeAmount: number): Observable<Budget> {
        return this.http.httpPost<Budget>(new HttpRequestData()
            .withUrl('/api/budget')
            .withBody({
                groupId: groupId,
                amount: amount,
                startDate: startDate,
                endDate: endDate,
                dateRange: dateRange,
                dateRangeAmount: dateRangeAmount,
            }))
    }

    getBudget(budgetId: number): Observable<Budget> {
        return this.http.httpGetSingle<Budget>(Budget, new HttpRequestData().withUrl('/api/budget/' + budgetId));
    }

    deleteBudget(budgetId: number): Observable<void> {
        return this.http.httpDelete(new HttpRequestData().withUrl('/api/budget/' + budgetId));
    }

    getHistoricalValues(startDate: moment.Moment, endDate: moment.Moment): Observable<HistoricalBudgetValue[]> {
      let startDateFormatted = startDate.format('yyyy-MM-DD');
      let endDateFormatted = (endDate == null ? moment() : endDate).format('yyyy-MM-DD');
      return this.http.httpGetList<HistoricalBudgetValue>(HistoricalBudgetValue,
          new HttpRequestData().withUrl('/api/money/transactions-group/historical-values/' + startDateFormatted + '/' + endDateFormatted));
    }

    updateAll() {
        this.http.basicHttpRequest(new HttpRequestData().withRequestMethod(HttpRequestMethod.GET).withUrl('/api/money/transactions-group/recalculate-historical-budget')).subscribe();
    }
}
