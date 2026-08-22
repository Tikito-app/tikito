---
sidebar_position: 1
---

# How to use Tikito

This page explains the main workflows once Tikito is installed and your initial data is imported.

## Navigation

The top navigation bar gives access to the main sections:

| Section | Description |
|---|---|
| **Overview** | Combined net worth across all accounts over time |
| **Money** | Bank account balances, transaction history, and spending groups |
| **Securities** | Investment portfolio — holdings, performance, and charts |
| **Money groups** | Create and manage transaction categories |
| **Import** | Upload bank or broker export files |
| **Loans** | Mortgage and loan tracking |
| **Admin** | Manage securities, users, jobs, and logs |

## Day-to-day usage

Tikito is designed to run continuously in the background. Security prices and currency exchange rates update automatically every night — you do not need to do anything.

When your bank or broker has new transactions:

1. Export the latest transactions from your bank or broker portal.
2. Click **Import** in Tikito and upload the file.
3. Tikito imports the transactions and queues background jobs to recalculate values.
4. Check **Admin → Jobs** to confirm processing completed.

## Adjusting the graphs

Each graph has controls to filter by date range and by account. You can also toggle:

- **Start at zero** — reset the y-axis to zero at the start of the selected range
- **Show groups** — overlay spending category lines on the money graph
- **Show closed positions** — include holdings where you have sold all shares

Your filter settings are saved as user preferences and restored the next time you open the page.

## Transaction groups

Transaction groups let you see where your money is going. Create a group (e.g. "Groceries") with qualifiers that match transaction descriptions or counterparty names, then go to **Admin → Money** to apply the grouping to existing transactions.

New transactions are matched against groups automatically when they are imported.

## Managing securities

After importing security transactions for the first time, visit **Admin → Securities** to enrich new securities. Enrichment fetches the market symbol, historical prices, and metadata (sector, industry, logo). You only need to do this once per security.

## Multi-user support

Tikito supports multiple user accounts. The admin user can create additional users from **Admin → Users**. Each user has their own accounts, holdings, and preferences.

## Changing preferences

Click your username in the top-right corner to access your preferences, including:

- Display language
- Default currency
- Graph date range defaults
