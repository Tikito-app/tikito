---
sidebar_position: 2
---

# Importing security transactions

Tikito can import investment transaction exports from DeGiro and accepts a generic CSV or Excel format for any other broker.

## Supported brokers

| Broker | Format |
|---|---|
| [DeGiro](https://www.degiro.nl/helpdesk/belasting/welke-rapportagemogelijkheden-zijn-er-en-waar-kan-ik-de-rapportages-vinden) | CSV (Account export + Transactions export) |

## Generic import (custom file)

If your broker is not supported, you can create a CSV or Excel file with the following columns:

| Column name | Required | Description |
|---|---|---|
| `date` | Yes | Date of the transaction (e.g. `25-06-2025`) |
| `time` | No | Time of the transaction (e.g. `15:34`) |
| `isin` | Yes | ISIN of the security |
| `amount` | No | Number of shares bought or sold |
| `price` | No | Price per share |
| `transaction-costs` | No | Brokerage fee for the transaction |
| `admin-costs` | No | Administrative / custody fee |
| `currency` | Yes | Currency code (e.g. `EUR`, `USD`) |
| `exchange-rate` | No | Exchange rate to EUR on the transaction date |
| `buy-sell` | Yes | `BUY` or `SELL` |

**Example:**

| date | time | isin | amount | price | transaction-costs | admin-costs | currency | exchange-rate | buy-sell |
|---|---|---|---|---|---|---|---|---|---|
| 25-06-2025 | 15:34 | US0378331005 | 5 | 180.00 | -1.00 | | USD | 1.08 | BUY |

From the example above, Tikito creates:
- One BUY transaction for 5 shares at 180.00 USD each
- One transaction cost of -1.00 USD

You can import only costs (without amount/price) by leaving those columns empty.

## Price sign convention

For BUY and SELL transactions, Tikito automatically applies the correct sign (negative for buys, positive for sells). For cost entries (`transaction-costs`, `admin-costs`), Tikito uses the value as provided — enter a negative value for costs you pay and a positive value for costs that are returned to you.

## Transaction types

The full list of transaction types that Tikito recognises and their effect:

| Type | Effect on holding |
|---|---|
| `BUY`, `BUY_PRODUCT_CHANGE`, `BUY_ISIN_CHANGE` | Increases share count |
| `SELL`, `SELL_PRODUCT_CHANGE`, `SELL_ISIN_CHANGE` | Decreases share count |
| `DIVIDEND` | Income, no share count change |
| `TAX`, `COUNTRY_TAX`, `DIVIDEND_TAX` | Cost, no share count change |
| `TRANSACTION_COST`, `ADMIN_COSTS`, `AANSLUITKOSTEN` | Cost, no share count change |

Transaction types from broker exports that represent internal transfers (e.g. cash movements within the broker) are imported but do not affect calculations.

## Broker cash account transfers

When you buy shares you transfer money from your bank to the broker's cash account. This shows up as an inbound transfer in the broker's export. Similarly, dividends paid by the broker appear as credits. Tikito handles these cash flows through the money holding that is automatically associated with each security holding.

## Foreign currencies

When you buy a security denominated in a foreign currency (e.g. a US stock in USD), Tikito applies the exchange rate for that day to calculate the EUR-equivalent value. If no exchange rate is in the file, Tikito looks up the historical rate automatically.

## Duplicate detection

A transaction is considered a duplicate when a transaction with the same account, ISIN, timestamp, amount, price, type, and cash position already exists. Duplicates are flagged in the import results and excluded from calculations.

## After importing

After import, Tikito queues background [jobs](/docs/features/jobs) to:

1. Enrich any new securities (resolve ISIN → market symbol, fetch metadata)
2. Fetch historical prices for enriched securities
3. Recalculate [historical values](/docs/concepts/historical-value) for all affected holdings

You can monitor job progress under **Admin → Jobs**.
