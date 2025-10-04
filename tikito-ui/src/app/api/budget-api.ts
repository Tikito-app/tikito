import {HttpService} from "../service/http.service";
import {Observable} from "rxjs";
import {HttpRequestData} from "../dto/http-request-data";
import {BudgetDateRange} from "../dto/budget/budget-date-range";
import Budget from "../dto/budget/budget";
import {Injectable} from "@angular/core";
import {HistoricalBudgetValue} from "../dto/budget/historical-budget-value";
import {HttpRequestMethod} from "../dto/http-request-method";

@Injectable({
    providedIn: 'root'
})
export class BudgetApi {
    constructor(private http: HttpService) {
    }

    getBudgets(): Observable<Budget[]> {
        return this.http.httpGetList<Budget>(Budget, new HttpRequestData().withUrl('/api/budget'));
    }

    createOrUpdateBudget(id: number | null,
                         name: string,
                         amount: number,
                         startDate: string,
                         dateRange: BudgetDateRange,
                         dateRangeAmount: number,
                         groupIds: number[]): Observable<Budget> {
        return this.http.httpPost<Budget>(new HttpRequestData()
            .withUrl('/api/budget')
            .withBody({
                id: id != null ? id : null,
                name: name,
                amount: amount,
                startDate: startDate,
                dateRange: dateRange,
                dateRangeAmount: dateRangeAmount,
                groupIds: groupIds
            }))
    }

    getBudget(budgetId: number): Observable<Budget> {
        return this.http.httpGetSingle<Budget>(Budget, new HttpRequestData().withUrl('/api/budget/' + budgetId));
    }

    deleteBudget(budgetId: number): Observable<void> {
        return this.http.httpDelete(new HttpRequestData().withUrl('/api/budget/' + budgetId));
    }

    getHistoricalValues(): Observable<HistoricalBudgetValue[]> {
        return this.http.httpGetList<HistoricalBudgetValue>(HistoricalBudgetValue, new HttpRequestData().withUrl('/api/budget/historical-values'));
    }

    updateAll() {
        this.http.basicHttpRequest(new HttpRequestData().withRequestMethod(HttpRequestMethod.GET).withUrl('/api/budget/recalculate-historical-budget')).subscribe();
    }
}
