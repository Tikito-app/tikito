---
sidebar_position: 5
---

# Transaction

A transaction is a record of a change to a [money](/docs/concepts/money) holding or a [security](/docs/concepts/security) holding.

## Money transactions

A money transaction changes the balance of a money holding. Common examples:

- A salary credit
- A rent payment
- A transfer to or from a broker

Each money transaction has:

| Field | Description |
|---|---|
| Date / time | When the transaction occurred |
| Amount | Positive (credit) or negative (debit) |
| Currency | The currency of the amount |
| Exchange rate | Rate used to convert to the account's base currency (if different) |
| Description | The free-text description from the bank |
| Counterparty name | Name of the other party |
| Counterparty account number | IBAN or other identifier of the other party |

## Security transactions

A security transaction records a change to a security position. Types that affect your holding:

| Type | Description |
|---|---|
| `BUY` | Purchase of shares |
| `BUY_PRODUCT_CHANGE` | Buy following a product change (e.g. fund restructuring) |
| `BUY_ISIN_CHANGE` | Buy because the security changed its ISIN |
| `SELL` | Sale of shares |
| `SELL_PRODUCT_CHANGE` | Sell following a product change |
| `SELL_ISIN_CHANGE` | Sell because the security changed its ISIN |
| `DIVIDEND` | Dividend payment received |
| `TAX` | General tax deduction |
| `COUNTRY_TAX` | Withholding tax from the source country |
| `DIVIDEND_TAX` | Tax on dividend income |
| `TRANSACTION_COST` | Brokerage fee for executing a trade |
| `ADMIN_COSTS` | Administrative / custody fee |
| `AANSLUITKOSTEN` | Connection/registration costs (Dutch brokers) |

Transaction types that are present in broker exports but do not affect calculations (such as internal cash transfers within the broker) are imported and stored but ignored in performance calculations.

## Duplicate detection

Tikito marks an imported transaction as a duplicate when a transaction with identical attributes already exists. A transaction is considered unique by the combination of: account, ISIN, timestamp, amount, price, transaction type, and cash position. Duplicates are shown separately and excluded from calculations.

## Price sign convention

For BUY and SELL transactions, Tikito automatically applies the correct sign. For cost transactions (transaction costs, admin costs), Tikito uses the value as imported — a negative value means you paid, a positive value means the broker returned the cost to you.
