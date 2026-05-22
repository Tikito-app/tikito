package org.tikito.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.tikito.dto.AccountDto;
import org.tikito.entity.Account;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccountStepDefinitions extends BaseStepDefinitions {

    @Given("default accounts")
    public void default_accounts() {
        withDefaultAccounts();
    }

    @Then("accounts should be in the database:")
    public void accounts_in_database(final List<Map<String, String>> expected) {
        final List<AccountDto> persisted = accountRepository.findAll().stream().map(Account::toDto).toList();
        equals(expected, persisted, this::accountEquals);
    }

    private boolean accountEquals(final Map<String, String> expectedMap, final AccountDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return false;
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return false;
        }
        if (expectedMap.containsKey("name") && !Objects.equals(expectedMap.get("name"), persisted.getName())) {
            return false;
        }
        if (expectedMap.containsKey("accountNumber") && !Objects.equals(expectedMap.get("accountNumber"), persisted.getAccountNumber())) {
            return false;
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }

        return true;
    }
}
