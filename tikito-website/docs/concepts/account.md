---
sidebar_position: 1
---

# Account

An account represents a financial account you hold at a bank, broker, or other institution. It acts as a container that groups your [money](/docs/concepts/money) holdings and [security](/docs/concepts/security) holdings together.

## Properties

| Field | Description |
|---|---|
| Name | A label you choose, e.g. "ABN AMRO Checking" |
| Account number | Optional — the actual bank/broker account number, used for matching during import |
| Currency | The base currency of the account (e.g. EUR, USD) |

## Holdings per account

An account can hold multiple types of assets:

- **Money holdings** — a debit or savings balance, cash, or cryptocurrency balance
- **Security holdings** — positions in stocks, ETFs, or crypto that you trade through a broker

Each account can hold **at most one holding per currency or security**. For example, you cannot have two separate Euro balances inside the same account.

## Multiple accounts

You can create multiple accounts — one per bank account or broker — and Tikito will track each one separately. The [overview](/docs/concepts/aggregated-historical-value) dashboard aggregates all accounts together so you can see your total net worth.
