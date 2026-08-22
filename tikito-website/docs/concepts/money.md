---
sidebar_position: 3
---

# Money

A money holding represents a liquid financial position — a bank account balance or a cryptocurrency balance that you hold directly (not through a broker).

## Types of money holdings

- **Fiat** — a balance in a regular currency (EUR, USD, etc.), such as a bank account or savings account. Transactions increase or decrease the balance over time.
- **Cryptocurrency** — a crypto balance held directly (e.g. Bitcoin on Bitvavo), as opposed to a crypto position traded through a broker.

## Starting balance

You can set a starting balance on a money holding. This is useful when you do not have all historical transactions available — the starting balance anchors the balance from a known point in time so that subsequent imports produce the correct final balance.

Some bank export formats (e.g. ING) include a final balance field in each transaction row. When Tikito detects this, it uses that value instead of computing it from the starting balance offset.

## Balance calculation

Tikito calculates the running balance by summing all [transactions](/docs/concepts/transaction) in chronological order. Each transaction either adds to or subtracts from the balance. Transactions imported from your bank are automatically matched to the correct money holding based on the account number.

## Currency

Every money holding is denominated in a specific [currency](/docs/concepts/currency). Tikito converts all holdings to the base currency of the [account](/docs/concepts/account) they belong to when displaying totals.

## Grouping transactions

You can create [money groups](/docs/features/money-graph) to categorise your transactions by description, counterparty name, or counterparty account number. Groups make it easy to see spending patterns and can also be linked to a [loan](/docs/concepts/loan) to track mortgage or other loan payments.
