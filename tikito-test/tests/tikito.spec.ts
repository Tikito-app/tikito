import {test, expect, Page} from '@playwright/test';
import {DEBIT_ACCOUNT_NAME, SECURITY_ACCOUNT_NAME, TIKITO_URL} from "./constant";
import {
  click, clickOnTable,
  clickPlusButton, clickSaveButton,
  gotoAccounts,
  gotoGroupsAndBudget, gotoLoans,
  performLogin, selectMultiOption,
  selectOption,
  setText
} from "./auth.setup";

async function createAccount(page: Page, name: string, accountNumber: string, accountType: string, currency: string) {
  await clickPlusButton(page);

  await setText(page, 'name', name);
  await setText(page, 'account-number', accountNumber);

  await selectOption(page, 'account-type', accountType);
  await selectOption(page, 'currency', 'Euro');

  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
  // await page.waitForURL(`${TIKITO_URL}/account`);
}

test.describe('Tikito Application Tests', () => {
  test('should create accounts', async ({page}) => {
    await performLogin(page);

    await gotoAccounts(page);

    await createAccount(page, DEBIT_ACCOUNT_NAME, 'debit-123', 'Debit/credit', 'Euro');
    await createAccount(page, SECURITY_ACCOUNT_NAME, 'security-456', 'Security', 'Euro');

    // await expect(page.locator('text=Debit Account')).toBeVisible();
    // await expect(page.locator('text=Security Account')).toBeVisible();
  });


  test('should create money groups and budget', async ({page}) => {
    await performLogin(page);

    await gotoGroupsAndBudget(page);
    await clickPlusButton(page);
    await click(page, 'edit-group');

    await createMoneyGroup(page, 'Groceries', 'MONEY', '-400', '01-01-2000', 'MONTH', '1');
    await clickPlusButton(page);
    await createMoneyGroupQualifier(page, 'Description', 'Includes', 'test');

    await clickPlusButton(page);
    await click(page, 'edit-group');

    await createMoneyGroup(page, 'Loans group', 'LOAN', null, null, null, null);
    await clickPlusButton(page);
    await createMoneyGroupQualifier(page, 'Description', 'Includes', 'test');
  });


  test('should create loans', async ({page}) => {
    await performLogin(page);

    await gotoLoans(page);
    await clickPlusButton(page);

    // linear loan
    await createLoan(page, 'Linear loan', 'Loans group', 'MONTH');
    await clickPlusButton(page);
    await click(page, 'edit-loan-part');
    await createLoanPart(page, 'Linear part 1', '500000', 'MORTGAGE_LINEAR', '01/01/2020', '12/31/2050', 'Euro', '1500');
    await clickPlusButton(page);
    await createInterest(page, '01/01/2020', '12/31/2050', '5');

    // Annuity loan
  });
});

async function createInterest(page: Page, startDate: string, endDate: string, amount: string) {
  await setText(page, 'start-date', startDate);
  await setText(page, 'end-date', endDate);
  await setText(page, 'amount', amount);
  await clickSaveButton(page);
  await expect(page.getByText(amount)).toBeVisible();
}

async function createLoanPart(page: Page, name: string, amount: string, type: string, startDate: string, endDate: string, currency: string, repaymentAmount: string) {
  await setText(page, 'name', name);
  await setText(page, 'amount', amount);
  await selectOption(page, 'loan-type', type);
  if(repaymentAmount != null) {
    await setText(page, 'repayment-amount', repaymentAmount);
  }
  await setText(page, 'start-date', startDate);
  await setText(page, 'end-date', endDate);
  await selectOption(page, 'currency', currency);
  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}

async function createLoan(page: Page, name: string, group: string, dateRange: string) {
  await setText(page, 'name', name);
  await selectMultiOption(page, 'groups', group);
  await selectOption(page, 'date-range', dateRange);

  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}

async function createMoneyGroupQualifier(page: Page, field: string, qualifierType: string, qualifier: string) {
  await selectOption(page, 'transaction-field', field);
  await selectOption(page, 'qualifier-type', qualifierType);
  await setText(page, 'qualifier', qualifier);

  await clickSaveButton(page);
  await expect(page.getByText(qualifier)).toBeVisible();
}

async function createMoneyGroup(page: Page, name: string, groupType: string, budgeted: string, startDate: string, dateRange: string, dateRangeAmount: string) {
  await setText(page,'name', name);
  await selectMultiOption(page, 'account-ids', DEBIT_ACCOUNT_NAME)
  await selectMultiOption(page, 'group-type', groupType)
  if(budgeted != null) {
    await setText(page, 'budgeted', budgeted);
  }
  if(startDate != null) {
    await setText(page, 'start-date', startDate);
  }
  if(dateRange != null) {
    await selectOption(page, 'date-range', dateRange);
  }
  if(dateRangeAmount != null) {
    await setText(page, 'date-range-amount', dateRangeAmount);
  }

  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}
