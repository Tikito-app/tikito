---
sidebar_position: 1
---

# Importing money transactions

Tikito can import bank transaction exports from several Dutch banks and also accepts a generic CSV or Excel format for any other source.

## Supported file formats

| Format | Extension |
|---|---|
| CSV | `.csv` |
| Excel | `.xlsx` / `.xls` |
| MT940 | `.mt940` / `.txt` |

## Supported banks

| Bank | Format |
|---|---|
| [ABN AMRO](https://www.abnamro.nl/nl/zakelijk/internet-bankieren/bestanden-downloaden.html) | MT940, Excel |
| [ING](https://www.ing.nl/particulier/digitaal-bankieren/afschriften-downloaden) | Excel |
| [Bunq](https://www.bunq.com/) | CSV |
| [Bitvavo](https://bitvavo.com/) | CSV (crypto exchange) |

If your bank is not in the list, use the **generic import** described below.

## Generic import (custom headers)

If Tikito cannot automatically detect the source, it will prompt you to map the columns in your file to the expected fields. The following fields are available:

| Column name | Required | Description |
|---|---|---|
| `timestamp` | Yes | Date (and optionally time) of the transaction |
| `time` | No | Time of the transaction (if separate from date) |
| `amount` | Yes | Transaction amount (positive = credit, negative = debit) |
| `currency` | No | Currency code (e.g. `EUR`, `USD`). Defaults to account currency |
| `debit-credit` | No | `D` or `C` indicator (debit/credit) as an alternative to signed amounts |
| `final-balance` | No | Running balance after this transaction |
| `description` | No | Free-text description of the transaction |
| `counterparty-account-name` | No | Name of the other party |
| `counterparty-account-number` | No | IBAN or account number of the other party |
| `exchange-rate` | No | Exchange rate to the account's base currency |

Map each column in your file to one of the names above when prompted during import.

## Duplicate detection

A transaction is considered a duplicate if an existing transaction with identical values already exists for the same account, timestamp, amount, and description. Duplicates are shown in the import results and excluded from calculations.

## After importing

After a successful import, Tikito queues a background [job](/docs/features/jobs) to recalculate the historical balance values for the affected money holding. The graph will update once the job completes.

You can also create [money groups](/docs/features/money-graph) to categorise the imported transactions.
