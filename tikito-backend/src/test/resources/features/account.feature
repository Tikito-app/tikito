Feature: the version can be retrieved

  Scenario: client makes call to GET /version
    Given default data
    Then accounts should be in the database:
      | userId | name                         | accountNumber | currency |
      | 1      | Test money account           | 1122          | EUR      |
      | 1      | Test security account        | 12345         | EUR      |
      | 1      | Test Dollar security account | 67890         | USD      |

    Then securities persisted are:
      | name           |
      | WOLTERS KLUWER |

