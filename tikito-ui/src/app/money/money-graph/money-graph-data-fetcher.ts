import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {MoneyGraphDto} from "./money-graph-dto";
import {MoneyTransactionsFilter} from "../../dto/money/money-transactions-filter";
import {MoneyApi} from "../../api/money-api";

@Injectable({
  providedIn: 'root',
})
export class MoneyGraphDataFetcher {

  constructor(private api: MoneyApi) {
  }

  assertHasData(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter): Observable<void> {
    return new Observable(observer => {
      this.assertHasMoneyTransactionGroups(dataDto).subscribe(() => {
        this.assertHasTransactions(dataDto, transactionFilter).subscribe(() => {
          this.assertHasBudget(dataDto, transactionFilter).subscribe(() => {
            this.assertHasHistoricalCashValues(dataDto, transactionFilter).subscribe(() => {
              observer.next();
            });
          });
        });
      });
    });
  }

  assertHasMoneyTransactionGroups(dataDto: MoneyGraphDto): Observable<void> {
    return new Observable(observer => {
      if (dataDto.moneyTransactionGroups != null) {
        observer.next();
      }
      this.api.getMoneyTransactionGroups().subscribe(groups => {
        dataDto.moneyTransactionGroups = groups;
        observer.next();
      });
    });
  }


  assertHasTransactions(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter): Observable<void> {
    return new Observable(observer => {
      if ((dataDto.moneyTransactionsInRange != null && dataDto.moneyTransactionsInRange.length > 0) || !transactionFilter.includeMoney) {
        observer.next();
      } else {
        let fetchWithSelectedDate = transactionFilter.startAtZeroFromBeginning || transactionFilter.startAtZeroAfterDateAggregation;
        this.api.getTransactions(fetchWithSelectedDate ? transactionFilter : transactionFilter.withoutStartDate()).subscribe(moneyTransactionsInRange => {
            dataDto.moneyTransactionsInRange = moneyTransactionsInRange;
            observer.next();
          });
      }
    });
  }

  assertHasBudget(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter): Observable<void> {
    dataDto.historicalBudgetValuesInRange = [];
    return new Observable(observer => {
      if (!transactionFilter.includeBudget || !transactionFilter.startAtZeroFromBeginning || !transactionFilter.startAtZeroAfterDateAggregation) {
        observer.next();
        return;
      }

      this.api.getHistoricalBudgetValues(transactionFilter.getStartDate(), transactionFilter.getEndDate()).subscribe(historicalBudgetValues => {
        dataDto.historicalBudgetValuesInRange = historicalBudgetValues
          .filter(value => transactionFilter.groupIds == null || transactionFilter.groupIds?.length == 0 || transactionFilter.groupIds.includes(value.groupId));
        observer.next();
      });
    });
  }

  assertHasHistoricalCashValues(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter): Observable<void> {
    return new Observable(observer => {
      if (!transactionFilter.includeMoneyHolding) {
        observer.next();
        return;
      }
      this.api.getHistoricalMoneyValues(transactionFilter)
        .subscribe(historicalCashValues => {
          dataDto.historicalCashValues = historicalCashValues;

          observer.next();
        })
    });
  }
}