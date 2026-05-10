import {Injectable} from "@angular/core";
import moment from "moment/moment";
import {MoneyBudgetTransaction} from "../../dto/money/money-budget-transaction";
import {MoneyGraphService} from "../../service/money-graph-service";
import {MoneyGraphDto} from "./money-graph-dto";
import {MoneyTransactionsFilter, TransactionDateRange} from "../../dto/money/money-transactions-filter";
import {HistoricalBudgetValue} from "../../dto/money/historical-budget-value";
import {MoneyGraphGroupInfo} from "../../dto/money/money-graph-group-info";
import {MoneyGraphGroupKey} from "../../dto/money/money-graph-group-key";
import MoneyTransaction from "../../dto/money/money-transaction";
import {MoneyGraphValue} from "../../dto/money/money-graph-value";
import {CacheService} from "../../service/cache-service";
import {Moment} from "moment";

@Injectable({
  providedIn: 'root',
})
export class MoneyGraphProcessor {

  static generateHistoricalCashValuesByCurrencyAndDate(dataDto: MoneyGraphDto) {
    dataDto.historicalCashValuesByCurrencyAndDate = {};
    dataDto.historicalCashValues.forEach(value => {
      if (dataDto.historicalCashValuesByCurrencyAndDate[value.currencyId] == null) {
        dataDto.historicalCashValuesByCurrencyAndDate[value.currencyId] = {}
      }
      let dateFormatted = moment(value.date).format('DD-MM-yyyy').toString();
      if (dataDto.historicalCashValuesByCurrencyAndDate[value.currencyId][dateFormatted] == null) {
        dataDto.historicalCashValuesByCurrencyAndDate[value.currencyId][dateFormatted] = value;
      } else {
        dataDto.historicalCashValuesByCurrencyAndDate[value.currencyId][dateFormatted].amount += value.amount;
      }
    });

    return dataDto.historicalCashValuesByCurrencyAndDate;
  }


  static generateMoneyBudgetTransactions(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter) {
    dataDto.moneyTransactionsWithBudget = [];

    if (transactionFilter.includeMoney && dataDto.moneyTransactionsInRange) {
      dataDto.moneyTransactionsWithBudget = dataDto.moneyTransactionsInRange.map(transaction => {
        const t = {...transaction} as MoneyBudgetTransaction;
        t.budgeted = undefined as any;
        return t;
      });
    }

    if (transactionFilter.includeBudget && dataDto.historicalBudgetValuesInRange) {
      dataDto.moneyTransactionsWithBudget = dataDto.moneyTransactionsWithBudget.concat(
        dataDto.historicalBudgetValuesInRange.map(budgetValue => {
          let groupName = dataDto.groupNameByGroupId[budgetValue.groupId]
          return MoneyGraphProcessor.mapToMoneyBudgetTransaction(budgetValue, groupName);
        }));
    }
    dataDto.moneyTransactionsWithBudget.sort((a, b) => moment(a.timestamp).unix() - moment(b.timestamp).unix());
  }

  static processMoneyGroups(dataDto: MoneyGraphDto) {
    let existingMoneyTransactionGroups: any = {}
    dataDto.moneyTransactionsWithBudget.forEach(value => {
      existingMoneyTransactionGroups[value.groupId] = true;
    });

    dataDto.moneyTransactionGroups
      .filter(group => existingMoneyTransactionGroups[group.id] != null)
      .forEach(group => {
        let groupInfo = new MoneyGraphGroupInfo(group.id, group.name, false, false, 0);

        dataDto.moneyGroupsByKey[groupInfo.key] = groupInfo;
        dataDto.groupNameByGroupId[group.id] = group.name;
      });
  }

  static generateOtherGroupsByName(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter) {
    if (transactionFilter.amountOfOtherGroups < 0) {
      return;
    }

    // now put all the non-grouped values in it by the counterparty name
    dataDto.moneyTransactionsWithBudget
      .filter(value => value.groupId == null || value.budgeted != null) // filter out grouped transactions and budgets
      .filter(value => !MoneyGraphService.isCrypto(value)) // filter out crypto
      .forEach(value => {
        const groupName = MoneyGraphProcessor.getGroupName(value, dataDto.groupNameByGroupId);
        const key = new MoneyGraphGroupKey(groupName, false, false).toString();

        if (!dataDto.moneyGroupsByKey[key]) {
          const id = value.groupId != null ? value.groupId : -1;
          dataDto.moneyGroupsByKey[key] = new MoneyGraphGroupInfo(id, groupName, false, false, value.currencyId);
        }
      });
  }

  /**
   * Sums up the positive value for each grouped and non-grouped value in the groupsByName. We only do this for the
   * period within the selection, because we use the normalizedMutatedAmount to split between top groups and other.
   */
  static calculateNormalizedAggregatedGroupValues(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter) {
    // Align filter dates to period boundaries for consistent weight calculation
    // todo: Do we need to calculate the values < startDate? Because of startAtZeroFromBeginning maybe?
    let alignedStartDate = MoneyGraphService.getDateByDateRange(transactionFilter.getStartDate(), transactionFilter);
    let alignedEndDate = transactionFilter.getEndDate() == null ? MoneyGraphService.getDateByDateRange(transactionFilter.getEndDate(), transactionFilter).endOf(MoneyGraphService.getPeriodUnit(transactionFilter)) : null;

    dataDto.moneyTransactionsWithBudget
      .filter(value => alignedStartDate == null || moment(value.timestamp).isSameOrAfter(alignedStartDate))
      .filter(value => alignedEndDate == null || moment(value.timestamp).isSameOrBefore(alignedEndDate))
      .forEach(value => {
        const isBudget = value.budgeted != null;
        const key = new MoneyGraphGroupKey(MoneyGraphProcessor.getGroupName(value, dataDto.groupNameByGroupId), isBudget, false).toString();

        if (dataDto.moneyGroupsByKey[key]) {
          dataDto.moneyGroupsByKey[key].normalizedMutatedAmount += Math.abs(value.amount * value.exchangeRate);
        }
      });
  }

  /**
   * Split the groupsByName into the highest valued non-grouped values. Then, add the
   * grouped values as well.
   */
  static extractHighestGroupsByKey(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter) {
    dataDto.highestGroupsByKey = {};

    // first limit to all non-grouped groups and then sort, because moneyGroupsByKey contains both
    // grouped values non-grouped values.
    Object.values(dataDto.moneyGroupsByKey)
      .filter((group: any) => group.id == -1)
      .sort((group1: any, group2: any) => {
        if (group1.normalizedMutatedAmount < group2.normalizedMutatedAmount) {
          return 1;
        } else if (group1.normalizedMutatedAmount > group2.normalizedMutatedAmount) {
          return -1;
        }
        return 0;
      })
      .slice(0, transactionFilter.amountOfOtherGroups)
      .forEach((group: any) => dataDto.highestGroupsByKey[group.key] = group);

    // now add the user defined groups
    Object.values(dataDto.moneyGroupsByKey)
      .filter((group: any) => group.id != -1)
      .forEach((group: any) => {
        dataDto.highestGroupsByKey[group.key] = group;
      });
  }


  static mapTransactionsToGraphValues(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter) {
    let dateRangeFormat = MoneyGraphService.getDateRangeFormat(transactionFilter);
    dataDto.moneyGraphValues = dataDto.moneyTransactionsWithBudget
      .filter(value => !MoneyGraphService.isCrypto(value))
      .map(value => {
        let date = moment(value.timestamp);
        let dateRangeString = date.format(dateRangeFormat); // format
        let dateRange = MoneyGraphService.getDateByDateRange(date, transactionFilter);
        let isBudget = value.budgeted != null;

        return new MoneyGraphValue(
          dateRangeString,
          dateRange,
          value.amount,
          new MoneyGraphGroupKey(MoneyGraphProcessor.getGroupName(value, dataDto.groupNameByGroupId), isBudget, false).toString(),
          value.currencyId);
      });
  }

  static sumValuesPerGroupAndDateRange(dataDto: MoneyGraphDto, otherGroupName: string) {
    let previousValuesPerGroup: any = {};

    dataDto.moneyGraphValues
      .forEach(graphValue => {
        const groupKeyObject = MoneyGraphGroupKey.fromString(graphValue.groupKey);
        const moneyGroupKeyObject = new MoneyGraphGroupKey(groupKeyObject.name, false, groupKeyObject.isHolding);

        let isLowestGroupValue = dataDto.highestGroupsByKey[moneyGroupKeyObject.toString()] == null;

        if (isLowestGroupValue) {
          let groupKey = new MoneyGraphGroupKey(otherGroupName, groupKeyObject.isBudget, false);
          graphValue.groupKey = groupKey.toString();
          MoneyGraphProcessor.addGroupIfNotExists(dataDto, groupKey)
        }

        let dateString = graphValue.dateRangeString;
        if (dataDto.moneyValuesPerGroupAndDateRange[graphValue.groupKey] == null) {
          dataDto.moneyValuesPerGroupAndDateRange[graphValue.groupKey] = {};
        }
        if (dataDto.moneyValuesPerGroupAndDateRange[graphValue.groupKey][dateString] == null) {
          dataDto.moneyValuesPerGroupAndDateRange[graphValue.groupKey][dateString] = new MoneyGraphValue(
            dateString,
            graphValue.date,
            0,
            graphValue.groupKey,
            graphValue.currencyId);
          previousValuesPerGroup[graphValue.groupKey] = dataDto.moneyValuesPerGroupAndDateRange[graphValue.groupKey][dateString];
        }
        dataDto.moneyValuesPerGroupAndDateRange[graphValue.groupKey][dateString].value += graphValue.value;
      });
  }

  static setAmountWhenNotStartAtZero(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter, firstDateOfData: Moment | null, firstDateToRender: moment.Moment) {
    if(transactionFilter.startAtZeroFromBeginning || firstDateOfData == null) {
      return ;
    }

    let currentDate = firstDateOfData.clone();
    let dateRangeFormat = MoneyGraphService.getDateRangeFormat(transactionFilter);
    let totalValuePerGroupBeforeStart: any = {};
    let currencyPerGroup: any = {};

    while (currentDate.isBefore(firstDateToRender)) {
      let currentRangedString = currentDate.format(dateRangeFormat);
      Object.keys(dataDto.moneyValuesPerGroupAndDateRange)
        .forEach(key => {
          if(dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString] != null) {
            if(totalValuePerGroupBeforeStart[key] == null) {
              totalValuePerGroupBeforeStart[key] = 0;
              currencyPerGroup[key] = 0;
            }
            totalValuePerGroupBeforeStart[key] += dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString].value;
            currencyPerGroup[key] = dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString].currencyId;
          }

        })

      currentDate = MoneyGraphProcessor.calculateNextCurrentDate(currentDate, transactionFilter);
    }

    let currentRangedString = currentDate.format(dateRangeFormat);
    for(let key of Object.keys(totalValuePerGroupBeforeStart)) {
      if(dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString] == null) {
        dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString] = new MoneyGraphValue(
          currentRangedString,
          currentDate,
          totalValuePerGroupBeforeStart[key],
          key,
          currencyPerGroup[key]);
      } else {
        dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString].value += totalValuePerGroupBeforeStart[key];
      }
    }
  }

  static fillInGapsForGroups(dataDto: MoneyGraphDto, transactionFilter: MoneyTransactionsFilter, firstDateToRender: any, lastDateToRender: any) {
    if(transactionFilter.startAtZeroAfterDateAggregation) {
      return ;
    }

    let currentDate = firstDateToRender.clone();
    let dateRangeFormat = MoneyGraphService.getDateRangeFormat(transactionFilter);
    let previousRangedString: any = null;

    while (currentDate.isSameOrBefore(lastDateToRender)) {
      let currentRangedString = currentDate.format(dateRangeFormat);
      Object.keys(dataDto.moneyValuesPerGroupAndDateRange)
        .forEach(key => {
          if(dataDto.moneyValuesPerGroupAndDateRange[key][previousRangedString] != null) {
            let previousGraphValue = dataDto.moneyValuesPerGroupAndDateRange[key][previousRangedString];
            if(dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString] == null) {
              dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString] = new MoneyGraphValue(currentRangedString, currentDate, 0, key, previousGraphValue.currencyId);
            }

            dataDto.moneyValuesPerGroupAndDateRange[key][currentRangedString].value += previousGraphValue.value
          }
        })

      previousRangedString = currentRangedString;
      currentDate = MoneyGraphProcessor.calculateNextCurrentDate(currentDate, transactionFilter);
    }
  }


  static generateCashSeriesForDate(dataDto: MoneyGraphDto, currentDateString: string, currentRangedString: string) {
    if (dataDto.historicalCashValuesByCurrencyAndDate == null) {
      return;
    }

    for (let currencyId of Object.keys(dataDto.historicalCashValuesByCurrencyAndDate)) {
      let currency = CacheService.getCurrencyById(currencyId as unknown as number);
      let value = dataDto.historicalCashValuesByCurrencyAndDate[currencyId][currentDateString];

      if (value != null) {
        let key = new MoneyGraphGroupKey(currency.name, false, true);
        if (dataDto.seriesPerGroupKey[key.toString()] == null) {
          dataDto.seriesPerGroupKey[key.toString()] = [];
          dataDto.cashHoldingValuesPerGroupAndDateRange[key.toString()] = {};
        }

        let exchangedValue = value.amount * value.currencyMultiplier;
        dataDto.seriesPerGroupKey[key.toString()].push(exchangedValue);
        dataDto.cashHoldingValuesPerGroupAndDateRange[key.toString()][currentRangedString] = value;
      }
    }
  }

  static getGroupName(transaction: MoneyTransaction, groupNamesById: any): string {
    if (transaction.groupId != null && groupNamesById[transaction.groupId]) {
      return groupNamesById[transaction.groupId];
    }
    if (transaction.counterpartyAccountName != null) {
      return transaction.counterpartyAccountName;
    }
    if (transaction.counterpartyAccountNumber != null) {
      return transaction.counterpartyAccountNumber;
    }
    return transaction.description;
  }

  static mapToMoneyBudgetTransaction(budgetValue: HistoricalBudgetValue, groupName: string): MoneyBudgetTransaction {
    let transaction = {...budgetValue} as unknown as MoneyBudgetTransaction;
    transaction.amount = budgetValue.budgeted;
    transaction.timestamp = budgetValue.date;
    transaction.counterpartyAccountName = groupName ? groupName : ('Group ' + budgetValue.groupId);
    transaction.budgeted = budgetValue.budgeted;
    transaction.groupId = budgetValue.groupId;
    return transaction;
  }

  static addGroupIfNotExists(dataDto: MoneyGraphDto, groupKey: MoneyGraphGroupKey) {
    if (dataDto.moneyGroupsByKey[groupKey.toString()] == null) {
      dataDto.moneyGroupsByKey[groupKey.toString()] = groupKey;
    }
  }


  static calculateNextCurrentDate(currentDate: moment.Moment, transactionFilter: MoneyTransactionsFilter) {
    switch (transactionFilter.dateRange) {
      case TransactionDateRange.YEAR:
        return currentDate.add(1, 'year').startOf('year');
      case TransactionDateRange.MONTH:
        return currentDate.add(1, 'month').startOf('month');
      case TransactionDateRange.WEEK:
        return currentDate.add(1, 'week').startOf('isoWeek');
      default:
        return currentDate.add(1, 'day');
    }
  }
}