import { defineConfig, devices } from '@playwright/test';
import path from 'path';

/**
 * Path to the authentication state file.
 */
// export const STORAGE_STATE = path.join(__dirname, 'playwright/.auth/user.json');

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({
  testDir: './tests',
  /* Run tests in files in parallel */
  fullyParallel: false,
  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: 'html',
  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    /* Base URL to use in actions like `await page.goto('')`. */
    baseURL: 'http://localhost:4200',

    /* Collect trace when retrying the failed test. See https://playwright.dev/docs/trace-viewer */
    trace: 'on-first-retry',
  },

  expect: {
    // timeout: 10_000,
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'initial-setup',
      testMatch: /initial-setup\.spec\.ts/,
    },
    {
      name: 'setup',
      testMatch: /auth.setup.setup\.ts/,
      // dependencies: ['initial-setup'],
    },
    {
      name: 'chromium',
      use: { 
        ...devices['Desktop Chrome'],
        // storageState: STORAGE_STATE,
      },
      dependencies: ['setup'],
      // testIgnore: /initial-setup\.spec\.ts/,
    },
    {
      name: 'firefox',
      use: { 
        ...devices['Desktop Firefox'],
        // storageState: STORAGE_STATE,
      },
      dependencies: ['setup'],
      // testIgnore: /initial-setup\.spec\.ts/,
    },
    {
      name: 'webkit',
      use: { 
        ...devices['Desktop Safari'],
        // storageState: STORAGE_STATE,
      },
      dependencies: ['setup'],
      // testIgnore: /initial-setup\.spec\.ts/,
    },
  ],
});
