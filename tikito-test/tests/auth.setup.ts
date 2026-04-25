import { test as setup, Page } from '@playwright/test';
import { TIKITO_URL } from "./constant";
import { STORAGE_STATE } from '../playwright.config';

export async function performLogin(page: Page) {
  await page.goto(`${TIKITO_URL}/login`);
  await page.fill('[data-cy="email"]', 'admin@example.com');
  await page.fill('[data-cy="password"]', 'Password123!');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForURL(`${TIKITO_URL}/`);
}

export async function gotoAccounts(page: Page) {
  // await page.getByRole('link', { name: 'Account' }).click();
  // await page.waitForURL(`${TIKITO_URL}/account`);
  await page.goto(`${TIKITO_URL}/account`);
}
export async function gotoGroupsAndBudget(page: Page) {
  await page.goto(`${TIKITO_URL}/money/transaction-group`);
}
export async function gotoLoans(page: Page) {
  await page.goto(`${TIKITO_URL}/loan`);
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

export async function clickSaveButton(page) {
  await clickButton(page, 'Save');
}

export async function click(page: Page, target: string) {
  await page.click('[data-cy="' + target + '"]');
}

// setup('authenticate', async ({ page }) => {
//   await performLogin(page);
//   await page.context().storageState({ path: STORAGE_STATE });
// });
