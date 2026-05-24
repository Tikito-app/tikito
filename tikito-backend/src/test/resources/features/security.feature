Feature: the version can be retrieved

  Scenario: client makes call to GET /version
    Given default data
    Given default security prices

    When security prices are:
      | security       | date       | price |
      | WOLTERS KLUWER | 2026-05-23 | 12.4  |
      | WOLTERS KLUWER | 2026-05-24 | 12.5  |

    When importing security transactions for user 1:
      | userId | account               | security       | timestamp              | amount | price | currency | exchangeRate | transactionType | description | cash |
      | 1      | Test security account | WOLTERS KLUWER | 2026-05-23T10:01:20.0Z | 5      | 12.4  | EUR      | 1            | BUY             | iets        | 0    |

    When historical value for user 1 and security 'WOLTERS KLUWER' are recalculated

    Then securities persisted are:
      | name           |
      | WOLTERS KLUWER |

    Then historical security holding values persisted have:
#      | security | date | amount | price | currencyMultiplier | totalDividend | totalAdministrativeCosts | totalTaxes | totalTransactionCosts | totalCashInvested | totalCashWithdrawn | worth | maxCashInvested | cashOnHand |
      | security       | date       | amount | price |
      | WOLTERS KLUWER | 2026-05-24 | 5      | 12.5  |

