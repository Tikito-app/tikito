package org.tikito.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.tikito.dto.AccountDto;
import org.tikito.entity.Account;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.when;

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

    @When("all jobs are finished")
    public void allJobsAreFinished() {
        // Write code here that turns the phrase above into concrete actions
        while (jobRepository.count() != 0) {
            jobService.processAllJobs();
        }
    }

    @Given("the current date is {string}")
    public void current_date_is(final String date) {
        when(timeService.now()).thenReturn(LocalDate.parse(date));
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
