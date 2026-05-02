import {Injectable} from "@angular/core";
import {CacheService} from "./cache-service";
import {MoneyGraphGroupKey} from "../dto/money/money-graph-group-key";
import {MoneyTransactionsFilter, TransactionDateRange} from "../dto/money/money-transactions-filter";
import moment from "moment/moment";
import {NormalizedMoneyGraphValue} from "../dto/money/normalized-money-graph-value";
import MoneyTransaction from "../dto/money/money-transaction";
import {SecurityType} from "../dto/security/security-type";
import {MoneyBudgetTransaction} from "../dto/money/money-budget-transaction";
import {Moment} from "moment";
import {HistoricalBudgetValue} from "../dto/money/historical-budget-value";
import {MoneyGraphGroupInfo} from "../dto/money/money-graph-group-info";


@Injectable({
  providedIn: 'root',
})
export class MoneyGraphService {

  /**
   * normalizedValues = [
   *   dateString: string;
   *   date: moment.Moment;
   *   value: number;
   *   groupName: string;
   * ];
   *
   * This variable contains all groups
   */
  static mapHistoricalMoneyValueToNormalizedMoneyValue(groupsById: any, allTransactions: MoneyBudgetTransaction[], transactionFilter: MoneyTransactionsFilter) {
    let format = MoneyGraphService.getDateRangeFormat(transactionFilter);
    return allTransactions
      .filter(value => !MoneyGraphService.isCrypto(value))
      .map(value => {
        let date = moment(value.timestamp);
        let dateRangeString = date.format(format); // format
        let dateRange = MoneyGraphService.getDateByDateRange(date, transactionFilter);
        const isBudget = value.budgeted != null;

        return new NormalizedMoneyGraphValue(
          dateRangeString,
          dateRange,
          value.amount, //this.applyExchangeRate(date, value.currencyId, value.amount),
          new MoneyGraphGroupKey(MoneyGraphService.getGroupNameOfHistoricalValue(value, groupsById), isBudget, false).toString(),
          value.currencyId,
          null);
      });
  }

  /**
   * Split the groupsByName into the highest valued non-grouped values. Then, add the
   * grouped values as well.
   */
  static splitGroupsAndMapByName(groupsByName: any, transactionFilter: MoneyTransactionsFilter) {
    let highestValuedGroups: any = {};

    // first limit to all non-grouped groups and then sort, because groupsByName contains both
    // grouped and non-grouped values.
    Object.values(groupsByName)
      .filter((group: any) => group.id == -1)
      .sort((group1: any, group2: any) => {
        if (group1.normalizedAggregatedValue < group2.normalizedAggregatedValue) {
          return 1;
        } else if (group1.normalizedAggregatedValue > group2.normalizedAggregatedValue) {
          return -1;
        }
        return 0;
      })
      .slice(0, transactionFilter.amountOfOtherGroups)
      .forEach((group: any) => highestValuedGroups[group.key] = group);

    // now add the user defined groups
    Object.values(groupsByName)
      .filter((group: any) => group.id != -1)
      .forEach((group: any) => {
        highestValuedGroups[group.key] = group;
      });

    return highestValuedGroups;
  }


  /**
   * Sums up the positive value for each grouped and non-grouped value in the groupsByName.
   */
  static calculateNormalizedAggregatedValues(allTransactions: MoneyBudgetTransaction[], groupsByName: any, groupsById: any, startDate: Moment | null, endDate: Moment | null, transactionFilter: MoneyTransactionsFilter) {
    // let startDate = this.getStartDate();
    // let endDate = this.getEndDate();

    // Align filter dates to period boundaries for consistent weight calculation
    let alignedStartDate = startDate ? MoneyGraphService.getDateByDateRange(startDate, transactionFilter) : null;
    let alignedEndDate = endDate ? MoneyGraphService.getDateByDateRange(endDate, transactionFilter).endOf(MoneyGraphService.getPeriodUnit(transactionFilter)) : null;

    allTransactions
      .filter(value => alignedStartDate == null || moment(value.timestamp).isSameOrAfter(alignedStartDate))
      .filter(value => alignedEndDate == null || moment(value.timestamp).isSameOrBefore(alignedEndDate))
      .forEach(value => {
        const isBudget = value.budgeted != null;
        const key = new MoneyGraphGroupKey(MoneyGraphService.getGroupNameOfHistoricalValue(value, groupsById), isBudget, false).toString();
        if (groupsByName[key]) {
          groupsByName[key].normalizedAggregatedValue += this.getPositiveValue(value.amount * value.exchangeRate);
        }
      });
  }

  static generateGroupValuesForMoney(historicalMoneyValuesByCurrencyAndDate: any, groupKeys: string[], seriesValuesByKey: any, groupValuePerDate: any, currentDateString: string) {
    if (historicalMoneyValuesByCurrencyAndDate == null) {
      return;
    }

    for (let currencyId of Object.keys(historicalMoneyValuesByCurrencyAndDate)) {
      let currency = CacheService.getCurrencyById(currencyId as unknown as number);
      let value = historicalMoneyValuesByCurrencyAndDate[currencyId][currentDateString];

      if (value != null) {
        let key = new MoneyGraphGroupKey(currency.name, false, true);
        if (seriesValuesByKey[key.toString()] == null) {
          seriesValuesByKey[key.toString()] = [];
          groupKeys.push(key.toString());
        }

        let exchangedValue = value.amount * value.currencyMultiplier;//this.applyExchangeRate(date, value.currencyId, value.amount);
        seriesValuesByKey[key.toString()].push(exchangedValue);
        groupValuePerDate[currentDateString][key.toString()] = exchangedValue;
      }
    }
  }

  static generateOtherGroupsByName(allTransactions: MoneyBudgetTransaction[], groupsByName: any, transactionFilter: MoneyTransactionsFilter) {
    // now put all the non-grouped values in it by the counterparty name
    allTransactions
      .filter(value => value.groupId == null || value.budgeted != null)
      .filter(value => !MoneyGraphService.isCrypto(value))
      .forEach(value => {
        const groupName = MoneyGraphService.getGroupNameOfHistoricalValue(value, transactionFilter);
        const isBudget = value.budgeted != null;
        const key = new MoneyGraphGroupKey(groupName, isBudget, false).toString();
        if (!groupsByName[key]) {
          const id = value.groupId != null ? value.groupId : -1;
          groupsByName[key] = new MoneyGraphGroupInfo(id, groupName, isBudget, false, value.currencyId);
        }
      });
  }

  static getGroupNameOfHistoricalValue(transaction: MoneyTransaction, groupsById: any): string {
    if (transaction.groupId != null && groupsById[transaction.groupId]) {
      return groupsById[transaction.groupId].name;
    }
    if (transaction.counterpartyAccountName != null) {
      return transaction.counterpartyAccountName;
    }
    if (transaction.counterpartyAccountNumber != null) {
      return transaction.counterpartyAccountNumber;
    }
    return transaction.description;
  }

  /**
   * Because we are dealing with transactions, it will happen that we have multiple
   * values per date range value. We must aggregate the values of those, in order to have a
   * single value per date range value. We also must sort by oldest first.
   */
  static aggregateValues(normalizedValues: NormalizedMoneyGraphValue[], highestValuedGroups: any, otherGroupName: string) {
    let valuesPerGroupAndDateRange: any = {};
    let previousValuesPerGroup: any = {};

    normalizedValues
      .forEach(value => {
        let isLowestGroupValue = highestValuedGroups[value.groupKey] == null;
        if (isLowestGroupValue) {
          const groupKeyObject = MoneyGraphGroupKey.fromString(value.groupKey);
          value.groupKey = new MoneyGraphGroupKey(otherGroupName, groupKeyObject.isBudget, false).toString();
        }

        let dateString = value.dateString;
        if (valuesPerGroupAndDateRange[value.groupKey] == null) {
          valuesPerGroupAndDateRange[value.groupKey] = {};
        }
        if (valuesPerGroupAndDateRange[value.groupKey][dateString] == null) {
          let startValue = previousValuesPerGroup[value.groupKey] == null ? 0 : previousValuesPerGroup[value.groupKey].value;
          valuesPerGroupAndDateRange[value.groupKey][dateString] = new NormalizedMoneyGraphValue(
            dateString,
            value.date,
            startValue,
            value.groupKey,
            value.currencyId,
            previousValuesPerGroup[value.groupKey]);
          previousValuesPerGroup[value.groupKey] = valuesPerGroupAndDateRange[value.groupKey][dateString];
        }
        valuesPerGroupAndDateRange[value.groupKey][dateString].value += value.value;
      });

    let aggregatedValuesPerDateRange: NormalizedMoneyGraphValue[] = [];
    Object
      .values(valuesPerGroupAndDateRange)
      .forEach((o: any) =>
        Object
          .values(o)
          .forEach((value: any) => aggregatedValuesPerDateRange.push(value)));

    aggregatedValuesPerDateRange.sort((value1, value2) => {
      if (value1.date.isAfter(value2.date)) {
        return 1;
      } else if (value2.date.isAfter(value1.date)) {
        return -1;
      }
      return 0;
    });
    return aggregatedValuesPerDateRange;
  }

  static getDateRangeFormat(transactionFilter: MoneyTransactionsFilter) {
    let range = transactionFilter.dateRange;
    if (range == TransactionDateRange.WEEK) {
      return 'YYYY-WW';
    } else if (range == TransactionDateRange.MONTH) {
      return 'YYYY-MM';
    } else if (range == TransactionDateRange.YEAR) {
      return 'YYYY';
    }

    return 'YYYY-MM-DD';
  }

  static getPeriodUnit(transactionFilter: MoneyTransactionsFilter) {
    switch (transactionFilter.dateRange) {
      case TransactionDateRange.YEAR:
        return 'year';
      case TransactionDateRange.MONTH:
        return 'month';
      case TransactionDateRange.WEEK:
        return 'isoWeek';
      default:
        return 'day';
    }
  }

  static getDateByDateRange(date: moment.Moment, transactionFilter: MoneyTransactionsFilter) {
    return date.clone().startOf(MoneyGraphService.getPeriodUnit(transactionFilter) as any);
  }

  static isCrypto(transaction: MoneyTransaction): boolean {
    let security = CacheService.getCurrencyById(transaction.currencyId as number);
    return security != null && security.securityType == SecurityType.CRYPTO;
  }

  static getPositiveValue(value: number): number {
    if (value == null || isNaN(value)) {
      return 0;
    }
    return value < 0 ? -value : value;
  }

  static mapToMoneyBudgetTransaction(budgetValue: HistoricalBudgetValue, groupsById: any): MoneyBudgetTransaction {
    let transaction = {...budgetValue} as unknown as MoneyBudgetTransaction;
    transaction.amount = budgetValue.budgeted;
    transaction.timestamp = budgetValue.date;
    const group = groupsById[budgetValue.groupId];
    transaction.counterpartyAccountName = group ? group.name : ('Group ' + budgetValue.groupId);
    transaction.budgeted = budgetValue.budgeted;
    transaction.groupId = budgetValue.groupId;
    return transaction;
  }
}