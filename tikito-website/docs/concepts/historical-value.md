---
sidebar_position: 6
---

# Historical value

The historical value of a [money](/docs/concepts/money) holding or [security](/docs/concepts/security) holding is the value of that holding on a specific date, expressed in the base currency of the [account](/docs/concepts/account) it belongs to.

## Money historical value

For money holdings, the historical value is the running balance on a given day. It is computed by starting from the initial balance (or zero if none is set) and applying all [transactions](/docs/concepts/transaction) up to and including that date.

## Security historical value

For security holdings, the historical value is the number of shares held multiplied by the market price of the security on that date, converted to the account's base currency using the exchange rate for that day.

Historical values for securities are calculated after:

- A new import is processed
- A manual "Recalculate historical value" action is triggered from the Admin panel
- The nightly price update job runs

## When is it recalculated?

Tikito recalculates historical values automatically via background [jobs](/docs/features/jobs) when you import new transactions or when nightly security prices are updated. You can also trigger a recalculation manually from the Admin panel for any individual security.
