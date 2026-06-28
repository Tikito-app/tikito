---
sidebar_position: 8
---

# Loan

A loan in Tikito represents a debt you are paying off over time, such as a mortgage or student loan. Tikito tracks the remaining balance and calculates expected future payments.

## Loan types

| Type | Description |
|---|---|
| **Annuity mortgage** (`MORTGAGE_ANNUITEIT`) | Fixed monthly payment; the interest portion decreases and principal portion increases over time |
| **Linear mortgage** (`MORTGAGE_LINEAR`) | Fixed principal repayment each period; total payment decreases over time as interest falls |
| **Student loan** (`STUDENT`) | Flexible repayment typically with income-based terms |
| **Generic** (`GENERIC`) | A generic loan for any other scenario |

## Loan parts

A single loan can consist of multiple **loan parts**, each with its own:

- Name
- Start and end dates
- Initial amount
- Currency
- Type (annuity, linear, etc.)
- Interest rate history (rates can change over the lifetime of the loan)

This is common for mortgages where a home is financed with multiple tranches at different interest rates.

## Connecting to money groups

A loan can be linked to one or more [money groups](/docs/features/money-graph). This allows Tikito to correlate your actual monthly bank payments with the scheduled loan payments, giving you a view of how closely your payments match the amortisation schedule.

## Remaining amount

Tikito tracks the remaining balance of each loan part. This is updated based on the amortisation schedule derived from the loan type, amount, interest rate, and term.
