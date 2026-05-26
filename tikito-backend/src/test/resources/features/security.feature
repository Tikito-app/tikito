Feature: the version can be retrieved

#  Scenario: Example import
#    Given default data
#    Given default security prices
#
#    When security prices are:
#      | security       | date       | price |
#      | WOLTERS KLUWER | 2026-05-23 | 12.4  |
#      | WOLTERS KLUWER | 2026-05-24 | 12.5  |
#
#    When importing security transactions for user 1:
#      | userId | account               | security       | timestamp              | amount | price | currency | exchangeRate | transactionType | description | cash |
#      | 1      | Test security account | WOLTERS KLUWER | 2026-05-23T10:01:20.0Z | 5      | 12.4  | EUR      | 1            | BUY             | iets        | 0    |
#
#    When historical value for user 1 and security 'WOLTERS KLUWER' are recalculated
#
#    Then securities persisted are:
#      | name           |
#      | WOLTERS KLUWER |
#
#    Then historical security holding values persisted have:
##      | security | date | amount | price | currencyMultiplier | totalDividend | totalAdministrativeCosts | totalTaxes | totalTransactionCosts | totalCashInvested | totalCashWithdrawn | worth | maxCashInvested | cashOnHand |
#      | security       | date       | amount | price | performance |
#      | WOLTERS KLUWER | 2026-05-24 | 5      | 12.5  | 10.5        |


  Scenario: Calculate performance for lots of cash changes
    Given default data
    Given default security prices
    Given the current date is '2025-01-10'

    When security prices are:
      | security       | date       | price |
      | WOLTERS KLUWER | 2025-01-01 | 10    |
      | WOLTERS KLUWER | 2025-01-02 | 10    |
      | WOLTERS KLUWER | 2025-01-03 | 10    |
      | WOLTERS KLUWER | 2025-01-04 | 20    |

    When importing security transactions for user 1:
      | userId | account               | security       | timestamp              | amount | price | currency | exchangeRate | transactionType  | cash |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-01T10:01:20.0Z | 10     | -10   | EUR      | 1            | BUY              | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-02T10:02:20.0Z | 10     | 10    | EUR      | 1            | SELL             | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-03T10:03:20.0Z | 10     | -10   | EUR      | 1            | BUY              | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-05T10:03:20.0Z | 10     | 25    | EUR      | 1            | DIVIDEND         | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-06T10:03:20.0Z | 10     | -5    | EUR      | 1            | COUNTRY_TAX      | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-07T10:03:20.0Z | 10     | -3    | EUR      | 1            | TRANSACTION_COST | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-08T10:03:20.0Z | 10     | -4    | EUR      | 1            | AANSLUITKOSTEN   | 0    |

    When all jobs are finished

    Then securities persisted are:
      | name           |
      | WOLTERS KLUWER |

    Then historical security holding values persisted have:
      | security       | date       | amount | price | performance | totalDividend | totalAdministrativeCosts | totalTaxes | totalTransactionCosts | totalCashInvested | totalCashWithdrawn | worth | maxCashInvested | cashOnHand |
      | WOLTERS KLUWER | 2025-01-01 | 10     | 10    | 0           | 0             | 0                        | 0          | 0                     | -100              | 0                  | 0     | -100            | 0          |
      | WOLTERS KLUWER | 2025-01-02 | 0      | 10    | 0           | 0             | 0                        | 0          | 0                     | -100              | 100                | 0     | -100            | 100        |
      | WOLTERS KLUWER | 2025-01-03 | 10     | 10    | 0           | 0             | 0                        | 0          | 0                     | -200              | 100                | 0     | -100            | 0          |
      | WOLTERS KLUWER | 2025-01-04 | 10     | 20    | 100         | 0             | 0                        | 0          | 0                     | -200              | 100                | 100   | -100            | 0          |
      | WOLTERS KLUWER | 2025-01-05 | 10     | 20    | 125         | 25            | 0                        | 0          | 0                     | -200              | 100                | 125   | -100            | 25         |
      | WOLTERS KLUWER | 2025-01-06 | 10     | 20    | 120         | 25            | 0                        | -5         | 0                     | -200              | 100                | 120   | -100            | 20         |
      | WOLTERS KLUWER | 2025-01-07 | 10     | 20    | 117         | 25            | 0                        | -5         | -3                    | -200              | 100                | 120   | -100            | 17         |
      | WOLTERS KLUWER | 2025-01-08 | 10     | 20    | 113         | 25            | -4                       | -5         | -3                    | -200              | 100                | 116   | -100            | 13         |

