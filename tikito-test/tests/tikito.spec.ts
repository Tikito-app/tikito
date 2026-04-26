import {expect, Page, test} from '@playwright/test';
import {DEBIT_ACCOUNT_NAME, SECURITY_ACCOUNT_NAME, TIKITO_URL} from "./constant";
import {
  performLogin,
} from "./auth.setup";
import {
  click,
  clickButton, clickOnTable,
  clickPlusButton, clickSaveButton,
  createInterest,
  createLoan,
  createLoanPart,
  createMoneyGroup,
  createMoneyGroupQualifier, goto, gotoAccounts, gotoGroupsAndBudget, gotoImport, gotoLoans,
  importFile, selectOption, setText
} from "./helpers";

async function createAccount(page: Page, name: string, accountNumber: string, accountType: string, currency: string) {
  await clickPlusButton(page);

  await setText(page, 'name', name);
  await setText(page, 'account-number', accountNumber);

  await selectOption(page, 'account-type', accountType);
  await selectOption(page, 'currency', 'Euro');

  await clickSaveButton(page);
  await expect(page.getByText(name)).toBeVisible();
}

test.describe('Tikito Application Tests', () => {

  test('should setup tikito (initial installation)', async ({page}) => {
    await page.goto(`${TIKITO_URL}/initial-installation`);

    await setText(page, 'email', 'admin@example.com');
    await setText(page, 'password', 'Password123!');
    await setText(page, 'password-again', 'Password123!');

    await clickButton(page, 'Register');

    await page.waitForURL(`${TIKITO_URL}/login`);
    await expect(page).toHaveURL(`${TIKITO_URL}/login`);
  });


  test('should create accounts', async ({page}) => {
    await performLogin(page);
    await gotoAccounts(page);

    await createAccount(page, DEBIT_ACCOUNT_NAME, 'debit-123', 'Debit/credit', 'Euro');
    await createAccount(page, SECURITY_ACCOUNT_NAME, 'security-456', 'Security', 'Euro');
  });


  test('should create money groups and budget', async ({page}) => {
    await performLogin(page);

    await gotoGroupsAndBudget(page);
    await clickPlusButton(page);
    await click(page, 'edit-group');

    await createMoneyGroup(page, 'Groceries', 'MONEY', '-400', '01-01-2000', 'MONTH', '1');
    await clickPlusButton(page);
    await createMoneyGroupQualifier(page, 'Description', 'Includes', 'test');

    await gotoGroupsAndBudget(page);
    await clickPlusButton(page);
    await click(page, 'edit-group');

    await createMoneyGroup(page, 'Linear loan group', 'LOAN', null, null, null, null);
    await clickPlusButton(page);
    await createMoneyGroupQualifier(page, 'Description', 'Includes', 'test');

    await createMoneyGroup(page, 'Annuity loan group', 'LOAN', null, null, null, null);
    await clickPlusButton(page);
    await createMoneyGroupQualifier(page, 'Description', 'Includes', 'test');
  });


  test('should create loans', async ({page}) => {
    await performLogin(page);

    await gotoLoans(page);
    await clickPlusButton(page);

    // linear loan
    await createLoan(page, 'Linear loan', 'Linear loan group', 'MONTH');
    await clickPlusButton(page);
    await click(page, 'edit-loan-part');
    await createLoanPart(page, 'Linear part 1', '500000', 'MORTGAGE_LINEAR', '01/01/2020', '12/31/2050', 'Euro', '1500');
    await clickPlusButton(page);
    await createInterest(page, '01/01/2020', '12/31/2050', '5');

    // Annuity loan
    await createLoan(page, 'Annuity loan', 'Annuity loan group', 'MONTH');
    await clickPlusButton(page);
    await click(page, 'edit-loan-part');
    await createLoanPart(page, 'Annuity part 1', '400000', 'MORTGAGE_ANNUITEIT', '01/01/2010', '12/31/2030', 'Euro', null);
    await clickPlusButton(page);
    await createInterest(page, '01/01/2010', '12/31/2030', '4');
  });


  test('should import files', async ({page}) => {
    await performLogin(page);
    await gotoImport(page);

    await importFile(page, DEBIT_ACCOUNT_NAME, '../resources/test_money_transactions.csv');
    await gotoImport(page);
    await importFile(page, SECURITY_ACCOUNT_NAME, '../resources/test_security_transactions.csv');
  });

  test('should update security symbol', async ({page}) => {
    await performLogin(page);
    await goto(page, '/admin/security');
    await page.waitForTimeout(2000);
    await setText(page, 'filter-field', 'Shell');
    await page.press('[data-cy="filter-field"]', 'Enter');
    await page.waitForTimeout(500);
    await click(page, 'option-button');
    await click(page, 'option-edit-isin');
    await clickOnTable(page, 'GB00BP6MXD84');
    await setText(page, 'symbol', 'new-shell-symbol');
    await setText(page, 'valid-from', '2019-01-31');
    await setText(page, 'valid-to', '2026-01-31');
    await clickSaveButton(page);

    await expect(page.getByText('new-shell-symbol')).toBeVisible();
    await expect(page.getByText('2019-01-31')).toBeVisible();
    await expect(page.getByText('2026-01-31')).toBeVisible();
  });
});

