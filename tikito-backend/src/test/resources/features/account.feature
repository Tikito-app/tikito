Feature: the version can be retrieved

  Scenario: client makes call to GET /version
    Given default data
    Then accounts should be in the database:
      | user        | name                         | accountNumber | currency |
      | tikito-user | Test money account           | 1122          | EUR      |
      | tikito-user | Test security account        | 12345         | EUR      |
      | tikito-user | Test Dollar security account | 67890         | USD      |

    Then securities persisted are:
      | name           |
      | WOLTERS KLUWER |

