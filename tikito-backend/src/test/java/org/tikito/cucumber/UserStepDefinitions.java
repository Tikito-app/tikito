package org.tikito.cucumber;

import io.cucumber.java.en.Given;

public class UserStepDefinitions extends BaseStepDefinitions {

    @Given("default currencies")
    public void default_currencies() {
        withDefaultCurrencies();
    }

    @Given("default user account")
    public void default_user_account() {
        withDefaultUserAccount();
    }

    @Given("default data")
    public void default_data() {
        withDefaultData();
    }

    @Given("default security prices")
    public void default_security_prices() {
//        withDefaultSecurityPrices();
    }

    @Given("logged in with the default user")
    public void logged_in_default_user() {
        loginWithDefaultUser();
    }
}
