import MoneyTransactionGroup from "../../dto/money/money-transaction-group";
import MoneyTransaction from "../../dto/money/money-transaction";
import {HistoricalBudgetValue} from "../../dto/money/historical-budget-value";
import {HistoricalMoneyHoldingValue} from "../../dto/money/historical-money-holding-value";
import {MoneyBudgetTransaction} from "../../dto/money/money-budget-transaction";
import {MoneyGraphValue} from "../../dto/money/money-graph-value";

export class MoneyGraphDto {
  moneyTransactionGroups: MoneyTransactionGroup[] = [];
  moneyTransactionsInRange: MoneyTransaction[] = [];
  historicalBudgetValuesInRange: HistoricalBudgetValue[] = [];
  historicalCashValues: HistoricalMoneyHoldingValue[] = [];
  moneyTransactionsWithBudget: MoneyBudgetTransaction[] = [];

  moneyGraphValues: MoneyGraphValue[];
  moneyValuesPerGroupAndDateRange: any = {};
  cashHoldingValuesPerGroupAndDateRange: any = {};

  moneyGroupsByKey: any = {};
  groupNameByGroupId: any = {};
  historicalCashValuesByCurrencyAndDate: any;
  highestGroupsByKey: any;

  seriesPerGroupKey: any;
  allDates: string[] = [];
}