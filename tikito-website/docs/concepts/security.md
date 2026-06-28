---
sidebar_position: 2
---

# Security

A security is a tradable financial asset that has a market price — a stock, ETF, or cryptocurrency traded through a broker.

## Security types

| Type | Description |
|---|---|
| **Stock** | A share in a company listed on a stock exchange |
| **ETF** | An exchange-traded fund tracking an index or basket of assets |
| **Crypto** | A cryptocurrency traded through a broker account (e.g. Bitcoin on DeGiro) |
| **Currency** | Used internally to represent fiat currencies; not imported directly |

## Identification (ISIN)

Every security is identified by an **ISIN** (International Securities Identification Number). ISINs are time-ranged — a security can have a different symbol over time if the company changes its ticker. Tikito stores the valid date range of each ISIN and always uses the currently active one.

After you import transactions for a security, Tikito needs to resolve its ISIN to a **market symbol** (e.g. `ASML` on Euronext) so it can fetch historical prices. You trigger this once via the Enrich action in the Admin panel.

## Broker account (cash position)

Every security holding has a corresponding [money](/docs/concepts/money) holding that represents the cash account at the broker. Transfers into the broker account, dividend payments, and transaction costs all flow through this cash position.

## Historical prices

Tikito fetches daily historical prices for each security. These are used to calculate the [historical value](/docs/concepts/historical-value) of your positions over time. Prices are updated automatically every night.

## Additional metadata

When you enrich a security, Tikito also retrieves:

- **Sector** — the sector the company operates in (e.g. Technology)
- **Industry** — the specific industry (e.g. Semiconductors)
- **Exchange** — the exchange where the security is listed
- **Logo / image**

## Performance calculation

The performance of a security holding is calculated from:

- The total amount of shares held
- The average buy price (weighted by transaction amounts)
- The current market price
- Dividends received
- Transaction and administrative costs paid

Tikito shows unrealised gain/loss and total return including dividends.
