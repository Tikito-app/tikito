// import { test, expect } from '@playwright/test';
// import {TIKITO_URL} from "./constant";
//
// test.describe('Initial Installation', () => {
//   // We don't want to use the global auth state for these tests
//   test.use({ storageState: { cookies: [], origins: [] } });
//
//   test('should setup tikito (initial installation)', async ({ page }) => {
//     await page.goto(`${TIKITO_URL}/initial-installation`);
//
//     await page.fill('[data-cy="email"]', 'admin@example.com');
//     await page.fill('[data-cy="password"]', 'Password123!');
//     await page.fill('[data-cy="password-again"]', 'Password123!');
//
//     await page.click('[data-cy="register-button"]');
//
//     await page.waitForURL(`${TIKITO_URL}/login`, { timeout: 10000 });
//     await expect(page).toHaveURL(`${TIKITO_URL}/login`);
//   });
//
//   test('should allow login after installation', async ({ page }) => {
//     await page.goto(`${TIKITO_URL}/login`);
//
//     await page.fill('[data-cy="email"]', 'admin@example.com');
//     await page.fill('[data-cy="password"]', 'Password123!');
//
//     await page.click('[data-cy="login-button"]');
//
//     await page.waitForURL(`${TIKITO_URL}/`, { timeout: 10000 });
//     await expect(page).toHaveURL(`${TIKITO_URL}/`);
//   });
// });
