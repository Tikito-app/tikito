package org.tikito.cucumber;

import io.cucumber.java.en.Given;

public class MoneyStepDefinitions extends BaseStepDefinitions {

    @Given("default money transaction groups")
    public void default_money_transaction_groups() {
        withDefaultMoneyTransactionGroups();
    }
}
