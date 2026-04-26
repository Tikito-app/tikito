import { Page } from '@playwright/test';
import { TIKITO_URL } from "./constant";

export async function performLogin(page: Page) {
  await page.goto(`${TIKITO_URL}/login`);
  await page.fill('[data-cy="email"]', 'admin@example.com');
  await page.fill('[data-cy="password"]', 'Password123!');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForURL(`${TIKITO_URL}/`);
}

