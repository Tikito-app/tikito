---
sidebar_position: 1
---

# Currency support

Tikito tracks holdings and transactions in their original currencies and converts values to a common display currency so you can compare everything in one place.

## Exchange rate handling

Each [transaction](/docs/concepts/transaction) stores the exchange rate that was applicable on the day it occurred. Tikito uses this rate to convert the original-currency value to Euros when calculating balances and historical values.

If no exchange rate is included in an import file, Tikito automatically looks up the historical rate for that currency on the transaction date.

## Nightly exchange rate updates

Currency exchange rates are refreshed automatically every night at 2 am. This keeps the conversion rates accurate for newly imported transactions and for the aggregated portfolio view.

## Display currency

Tikito currently displays all values in **Euros**. Support for choosing your preferred display currency is planned for a future release.

## Foreign currency transactions

When you import a transaction in a foreign currency (e.g. a USD stock purchase), Tikito applies the EUR/USD exchange rate for that day to calculate the Euro-equivalent value. Because exchange rates can vary throughout the day, the converted value is an approximation based on the daily rate.
