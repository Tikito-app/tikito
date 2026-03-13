export class ImportExportSettings {
  accounts: boolean = true;
  moneyTransactions: boolean = false;
  moneyTransactionGroups: boolean = false;
  securityTransactions: boolean = false;
  loans: boolean = false;

  constructor(accounts: boolean,
              moneyTransactions: boolean,
              moneyTransactionGroups: boolean,
              securityTransactions: boolean,
              loans: boolean) {
    this.accounts = accounts;
    this.moneyTransactions = moneyTransactions;
    this.moneyTransactionGroups = moneyTransactionGroups;
    this.securityTransactions = securityTransactions;
    this.loans = loans;
  }
}