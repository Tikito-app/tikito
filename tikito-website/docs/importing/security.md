---
sidebar_position: 1
---

# Security transactions
Tikito supports both a generic import of securities, as well as files from specific brokers.

## Broker export files
The current brokers that are supported are:
- [DeGiro](https://www.degiro.nl/helpdesk/belasting/welke-rapportagemogelijkheden-zijn-er-en-waar-kan-ik-de-rapportages-vinden)

## Custom file
If your broker is not supported, you can also create your own csv/Excel file and import it. 
An example structure is as follows:

| Date       | Time  | Isin   | Amount | Price | Transaction costs | Administrative costs | Currency | Exchange rate | Buy/sell |
|------------|-------|--------|--------|-------|-------------------|----------------------|----------|---------------|----------|
| 25-06-2025 | 15:34 | 123456 | 5      | 3.456 | 1.235             | 7.346                | EUR      | 1.156234      | BUY      |
|            |       |        |        |       |                   |                      |          |               |          |

Tikito will create three transactions from the above example.
- One transaction that buys 5 shares for 3.456 Euro.
- One transaction for the transaction costs of 7.346 Euro.
- One transaction for the administrative costs of 7.346 Euro.

You can also only import transaction costs or administrative costs by leaving the amount empty.

## Positive or negative price?
It is important to note that Tikito will take over the price for transactions as is in some cases. 
In case the type of transaction is a buy or sell of a security, Tikitio will change it to negative or positive automatically.

However, in case of other costs, such as transaction costs, Tikito will take over the price from the import. 
This is due to the fact that sometimes, brokers can actually return transaction costs to your account. 
Therefore, Tikito cannot determine if it should negate the price in those cases.
In case you pay for something, you should enter a negative price.

## Duplicate transactions
A transaction is marked as duplicate in case there are more transactions on the same exact day already present.
A transaction is unique if the following attributes are the same:
- account
- isin
- timestamp
- amount
- price
- transaction type
- cash position

## Foreign currencies
When you import a security with a foreign currency, we will apply the exchange rate that is set for that specific day.
We are aware that this might be different depending on the time of day. 
In the future Tikito will have support to set the exact exchange rate. 

## Transfer of money
When you want to buy shares, you need to transfer the money from your bank account to the bank account of the broker.
This will then be visible in your broker account, describing the cash flow. 
The same, but in opposite direction, happens when you e.g. receive dividends.
The dividend is transferred to your bank account at the broker. 
You then typically transfer it back to your own bank account.

## Transaction types
Tikito does not need all the possible transaction types. 
For example, DeGiro also exports transaction types with the description "Overboeking van uw geldrekening bij flatexDEGIRO Bank".
Tikito has a separate transaction type for those, but they will not impact the actual calculation of values.
The transaction types that are actually relevant are:
- BUY, BUY_PRODUCT_CHANGE, BUY_ISIN_CHANGE
- SELL, SELL_PRODUCT_CHANGE, SELL_ISIN_CHANGE
- DIVIDEND
- TAX, COUNTRY_TAX, DIVIDEND_TAX, TRANSACTION_COST, ADMIN_COSTS, AANSLUITKOSTEN