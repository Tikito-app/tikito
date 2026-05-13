import {expect, Page} from "@playwright/test";
import {DEBIT_ACCOUNT_NAME, TIKITO_URL} from "./constant";

export async function importFile(page: Page, accountName: string, assetType: string, filePath: string) {
  await selectOption(page, 'account', accountName);
  await selectOption(page, 'asset-type', assetType);
  await page.waitForTimeout(2000);

  await page.locator('[data-cy="file"]').click();
  await page.locator('[data-cy="file"]').setInputFiles(filePath);
  await clickButton(page, 'Continue');
  await clickButton(page, 'Import');
}

export async function createInterest(page: Page, startDate: string, endDate: string, amount: string) {
  await setText(page, 'start-date', startDate);
  await setText(page, 'end-date', endDate);
  await setText(page, 'amount', amount);
  await clickSaveButton(page);
  await expect(page.getByText(amount + '%')).toBeVisible();
}

export async function createLoanPart(page: Page, name: string, amount: string, type: string, startDate: string, endDate: string, currency: string, repaymentAmount: string) {
  await setText(page, 'name', name);
  await setText(page, 'amount', amount);
  await selectOption(page, 'loan-type', type);
  if (repaymentAmount != null) {
    await setText(page, 'repayment-amount', repaymentAmount);
  }
  await setText(page, 'start-date', startDate);
  await setText(page, 'end-date', endDate);
  await selectOption(page, 'currency', currency);
  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}

export async function createLoan(page: Page, name: string, group: string, dateRange: string) {
  await setText(page, 'name', name);
  await selectMultiOption(page, 'groups', group);
  await selectOption(page, 'date-range', dateRange);

  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}

export async function createMoneyGroupQualifier(page: Page, field: string, qualifierType: string, qualifier: string) {
  await selectOption(page, 'transaction-field', field);
  await selectOption(page, 'qualifier-type', qualifierType);
  await setText(page, 'qualifier', qualifier);

  await clickSaveButton(page);
  await expect(page.getByText(qualifier)).toBeVisible();
}

export async function createMoneyGroup(page: Page, name: string, groupType: string, budgeted: string, startDate: string, dateRange: string, dateRangeAmount: string) {
  await setText(page, 'name', name);
  await selectMultiOption(page, 'account-ids', DEBIT_ACCOUNT_NAME)
  await selectMultiOption(page, 'group-type', groupType)
  if (budgeted != null) {
    await setText(page, 'budgeted', budgeted);
  }
  if (startDate != null) {
    await setText(page, 'start-date', startDate);
  }
  if (dateRange != null) {
    await selectOption(page, 'date-range', dateRange);
  }
  if (dateRangeAmount != null) {
    await setText(page, 'date-range-amount', dateRangeAmount);
  }

  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}

export async function gotoAccounts(page: Page) {
  await page.goto(`${TIKITO_URL}/account`);
}
export async function gotoGroupsAndBudget(page: Page) {
  await page.goto(`${TIKITO_URL}/money/transaction-group`);
}
export async function gotoLoans(page: Page) {
  await page.goto(`${TIKITO_URL}/loan`);
}
export async function gotoImport(page: Page) {
  await page.goto(`${TIKITO_URL}/account/import`);
}
export async function gotoAdmin(page: Page) {
  await page.goto(`${TIKITO_URL}/admin`);
}
export async function goto(page: Page, link: string) {
  await page.goto(`${TIKITO_URL}` + link);
}

export async function clickPlusButton(page: Page) {
  await page.click('[data-cy="add"]');
}

export async function clickOnTable(page: Page, value: string) {
  await page.locator('.mat-mdc-cell')
    .filter({hasText: RegExp(`^${' ' + value + ' '}$`) })
    .click();
}

export async function selectOption(page: Page, target: string, value: string) {
  await page.locator('[data-cy="' + target + '"]').click();
  await page.locator('.mdc-list-item__primary-text')
    .filter({ hasText: RegExp(`^${value}$`) })
    .click();
}

export async function selectMultiOption(page: Page, target: string, value: string) {
  await selectOption(page, target, value);
  await page.keyboard.press('Escape');
  await page.keyboard.press('Escape');
}

export async function setText(page: Page, target: string, value: string) {
  await page.fill('[data-cy="' + target + '"]', value);
}

export async function clickButton(page: Page, value: string) {
  await page.getByRole('button', { name: value }).click();
}
export async function clickLink(page: Page, value: string) {
  await page.getByRole('link', { name: value }).click();
}

export async function clickSaveButton(page) {
  await clickButton(page, 'Save');
}

export async function click(page: Page, target: string) {
  await page.click('[data-cy="' + target + '"]');
}