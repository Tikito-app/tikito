---
sidebar_position: 2
---

# Money graph

The money graph visualises the balance of your [money](/docs/concepts/money) holdings over time.

## What it shows

The graph plots the [historical value](/docs/concepts/historical-value) of your money holdings day by day. You can see how your bank balances have grown, shrunk, or fluctuated over any time period.

## Filtering

You can filter the graph by:

- **Date range** — choose a preset (e.g. last year, last 5 years) or set a custom start and end date
- **Accounts** — include or exclude specific bank accounts
- **Groups** — show or hide specific transaction groups as overlay lines
- **Budget** — overlay your budgeted amounts on top of actual spending

## Transaction groups

Transaction groups let you categorise your money transactions so you can track spending by category (e.g. "Groceries", "Rent", "Salary"). Each group is defined by one or more qualifiers that match transactions by:

| Qualifier field | Description |
|---|---|
| Description | The free-text description on the transaction |
| Counterparty name | The name of the other party |
| Counterparty account number | The IBAN or account number of the other party |

Each qualifier can use one of three matching strategies:

| Strategy | Description |
|---|---|
| `INCLUDES` | The field contains the specified text |
| `SIMILAR` | The field is similar to the specified text (fuzzy match) |
| `REGEX` | The field matches the specified regular expression |

### Budgets

You can set a budgeted amount on a group to compare your actual spending against a target. The graph can overlay the budget line alongside actual values.

## Creating groups

Go to **Money groups** in the top navigation. Create a new group, give it a name, and add qualifiers. Once created, go to **Admin → Money** and apply the grouping to assign existing transactions to their groups.

## Loan payments

If a group is linked to a [loan](/docs/concepts/loan), Tikito overlays the expected loan payment schedule on the graph, so you can see whether your actual mortgage payments match the amortisation plan.
