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

    When security prices are:
      | security       | date       | price |
      | WOLTERS KLUWER | 2025-01-01 | 10    |
      | WOLTERS KLUWER | 2025-01-02 | 10    |
      | WOLTERS KLUWER | 2025-01-03 | 10    |
      | WOLTERS KLUWER | 2025-01-04 | 20    |

    When importing security transactions for user 1:
      | userId | account               | security       | timestamp              | amount | price | currency | exchangeRate | transactionType | description | cash |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-01T10:01:20.0Z | 10     | -10   | EUR      | 1            | BUY             | iets        | 0    |
      # simulate the cash going to 0 again
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-02T10:02:20.0Z | 10     | 10    | EUR      | 1            | SELL            | iets        | 0    |
      | 1      | Test security account | WOLTERS KLUWER | 2025-01-03T10:03:20.0Z | 10     | -10   | EUR      | 1            | BUY             | iets        | 0    |

    When all jobs are finished

    Then securities persisted are:
      | name           |
      | WOLTERS KLUWER |

    Then historical security holding values persisted have:
      | security       | date       | amount | price | performance | totalDividend | totalAdministrativeCosts | totalTaxes | totalTransactionCosts | totalCashInvested | totalCashWithdrawn | worth | maxCashInvested | cashOnHand |
      | WOLTERS KLUWER | 2025-01-01 | 10     | 10    | 0           | 0             | 0                        | 0          | 0                     | -100              | 0                  | 0     | -100            | 0          |
      | WOLTERS KLUWER | 2025-01-02 | 0      | 10    | 0           | 0             | 0                        | 0          | 0                     | -100              | 100                | 0     | -100            | 100        |
      | WOLTERS KLUWER | 2025-01-03 | 10     | 10    | 0           | 0             | 0                        | 0          | 0                     | -200              | 100                | 0     | -100            | 0          |
      | WOLTERS KLUWER | 2025-01-04 | 10     | 20    | 100         | 0             | 0                        | 0          | 0                     | -200              | 100                | 100   | -100            | 0        |

