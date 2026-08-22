---
sidebar_position: 4
---

# Currency

Every holding and transaction in Tikito is denominated in a specific currency. Tikito uses currency exchange rates to convert values across currencies so you can see totals in a single base currency.

## How exchange rates work

Each [transaction](/docs/concepts/transaction) stores its original currency and the exchange rate applicable on the day it occurred. When Tikito calculates the value of a holding on a specific date, it converts the original-currency value to Euros using the stored exchange rate.

If no exchange rate is present in an imported file, Tikito automatically looks up the historical exchange rate for the transaction's currency on that date.

## Default currency

Tikito currently converts all values to **Euros** by default. Support for choosing a different display currency is planned for a future release.

## Currency as a security

Internally, Tikito models fiat currencies and cryptocurrencies as a special `CURRENCY` security type. This allows exchange rates to be managed and updated the same way as security prices — via the nightly update job.

## Exchange rate updates

Currency exchange rates are refreshed automatically every night at 2 am. You can also trigger a manual refresh from the Admin panel.
