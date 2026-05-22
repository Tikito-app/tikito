package org.tikito.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.tikito.dto.security.*;
import org.tikito.entity.security.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SecurityStepDefinitions extends BaseStepDefinitions {


    @Given("default securities")
    public void default_securities() {
        withDefaultSecurities();
    }

    @When("historical value for user {int} and security {string} are recalculated")
    public void recalculate_historical_security_values(final int userId, final String securityName) {
        securityHoldingService.recalculateHistoricalValue(userId, BaseStepDefinitions.getSecurityId(securityName));
    }

    @When("aggregated historical security values for user {int} are recalculated")
    public void recalculate_historical_security_values(final int userId) {
        securityHoldingService.recalculateAggregatedHistoricalHoldingValues(userId);
    }

    @Then("securities persisted are:")
    public void securitiesAre(final List<Map<String, String>> expected) {
        final List<SecurityDto> persisted = securityRepository.findAll()
                .stream()
                .filter(security -> security.getSecurityType() == SecurityType.STOCK || security.getSecurityType() == SecurityType.ETF)
                .map(Security::toDto).toList();
        equals(expected, persisted, this::securityEquals);
    }

    @Then("security prices persisted are:")
    public void securityPricesAre(final List<Map<String, String>> expected) {
        final List<SecurityPriceDto> persisted = securityPriceRepository.findAll().stream().map(SecurityPrice::toDto).toList();
        equals(expected, persisted, this::securityPricEquals);
    }

    @Then("security transactions persisted are:")
    public void securityTransactionsAre(final List<Map<String, String>> expected) {
        final List<SecurityTransactionDto> persisted = securityTransactionRepository.findAll().stream().map(SecurityTransaction::toDto).toList();
        equals(expected, persisted, this::securityTransactionEquals);
    }

    @Then("security holdings persisted are:")
    public void securityHoldingsAre(final List<Map<String, String>> expected) {
        final List<SecurityHoldingDto> persisted = securityHoldingRepository.findAll().stream().map(SecurityHolding::toDto).toList();
        equals(expected, persisted, this::securityHoldingEquals);
    }

    @Then("historical security holding values persisted are:")
    public void historicalSecurityHoldingValuesAre(final List<Map<String, String>> expected) {
        final List<HistoricalSecurityHoldingValueDto> persisted = historicalSecurityHoldingValueRepository.findAll().stream().map(HistoricalSecurityHoldingValue::toDto).toList();
        equals(expected, persisted, this::historicalSecurityHoldingValueEquals);
    }

    @Then("aggregated historical security holding values persisted are:")
    public void aggregatedHistoricalSecurityHoldingValuesAre(final List<Map<String, String>> expected) {
        final List<AggregatedHistoricalSecurityHoldingValueDto> persisted = aggregatedHistoricalSecurityHoldingValueRepository.findAll().stream().map(AggregatedHistoricalSecurityHoldingValue::toDto).toList();
        equals(expected, persisted, this::aggregatedHistoricalSecurityHoldingValueEquals);
    }

    private boolean securityEquals(final Map<String, String> expectedMap, final SecurityDto persisted) {
        if (expectedMap.containsKey("securityType") && SecurityType.valueOf(expectedMap.get("securityType")) != persisted.getSecurityType()) {
            return false;
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap).longValue() != persisted.getCurrencyId().longValue()) {
            return false;
        }
        if (expectedMap.containsKey("name") && !Objects.equals(expectedMap.get("name"), persisted.getName())) {
            return false;
        }
        if (expectedMap.containsKey("sector") && !Objects.equals(expectedMap.get("sector"), persisted.getSector())) {
            return false;
        }
        if (expectedMap.containsKey("industry") && !Objects.equals(expectedMap.get("industry"), persisted.getIndustry())) {
            return false;
        }
        if (expectedMap.containsKey("exchange") && !Objects.equals(expectedMap.get("exchange"), persisted.getExchange())) {
            return false;
        }
        if (expectedMap.containsKey("imageUrl") && !Objects.equals(expectedMap.get("imageUrl"), persisted.getImageUrl())) {
            return false;
        }
        if (expectedMap.containsKey("currentIsin") && !Objects.equals(expectedMap.get("currentIsin"), persisted.getCurrentIsin())) {
            return false;
        }
        if (expectedMap.containsKey("lastPriceDate") && !Objects.equals(LocalDate.parse(expectedMap.get("lastPriceDate")), persisted.getLastPriceDate())) {
            return false;
        }

        return true;
    }

    private boolean securityPricEquals(final Map<String, String> expectedMap, final SecurityPriceDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return false;
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap).longValue() != persisted.getSecurityId()) {
            return false;
        }
        if (expectedMap.containsKey("date") && !Objects.equals(LocalDate.parse(expectedMap.get("date")), persisted.getDate())) {
            return false;
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return false;
        }

        return true;
    }

    private boolean securityTransactionEquals(final Map<String, String> expectedMap, final SecurityTransactionDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return false;
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return false;
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }
        if (expectedMap.containsKey("isin") && !Objects.equals(expectedMap.get("isin"), persisted.getIsin())) {
            return false;
        }
        if (expectedMap.containsKey("accountId") && Long.parseLong(expectedMap.get("accountId")) != persisted.getAccountId()) {
            return false;
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }
        if (expectedMap.containsKey("amount") && Integer.parseInt(expectedMap.get("amount")) != persisted.getAmount()) {
            return false;
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return false;
        }
        if (expectedMap.containsKey("exchangeRate") && Double.parseDouble(expectedMap.get("exchangeRate")) != persisted.getExchangeRate()) {
            return false;
        }
        if (expectedMap.containsKey("description") && !Objects.equals(expectedMap.get("description"), persisted.getDescription())) {
            return false;
        }
        if (expectedMap.containsKey("timestamp") && !Objects.equals(Instant.parse(expectedMap.get("timestamp")), persisted.getTimestamp())) {
            return false;
        }
        if (expectedMap.containsKey("transactionType") && SecurityTransactionType.valueOf(expectedMap.get("transactionType")) != persisted.getTransactionType()) {
            return false;
        }
        if (expectedMap.containsKey("cash") && Double.parseDouble(expectedMap.get("cash")) != persisted.getCash()) {
            return false;
        }

        return true;
    }

    private boolean securityHoldingEquals(final Map<String, String> expectedMap, final SecurityHoldingDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return false;
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return false;
        }
        if (expectedMap.containsKey("accountId") && Long.parseLong(expectedMap.get("accountId")) != persisted.getAccountId()) {
            return false;
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }
        if (expectedMap.containsKey("amount") && Integer.parseInt(expectedMap.get("amount")) != persisted.getAmount()) {
            return false;
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return false;
        }
        if (expectedMap.containsKey("totalDividend") && Double.parseDouble(expectedMap.get("totalDividend")) != persisted.getTotalDividend()) {
            return false;
        }
        if (expectedMap.containsKey("totalAdministrativeCosts") && Double.parseDouble(expectedMap.get("totalAdministrativeCosts")) != persisted.getTotalAdministrativeCosts()) {
            return false;
        }
        if (expectedMap.containsKey("totalTaxes") && Double.parseDouble(expectedMap.get("totalTaxes")) != persisted.getTotalTaxes()) {
            return false;
        }
        if (expectedMap.containsKey("totalTransactionCosts") && Double.parseDouble(expectedMap.get("totalTransactionCosts")) != persisted.getTotalTransactionCosts()) {
            return false;
        }
        if (expectedMap.containsKey("totalCashInvested") && Double.parseDouble(expectedMap.get("totalCashInvested")) != persisted.getTotalCashInvested()) {
            return false;
        }
        if (expectedMap.containsKey("totalCashWithdrawn") && Double.parseDouble(expectedMap.get("totalCashWithdrawn")) != persisted.getTotalCashWithdrawn()) {
            return false;
        }
        if (expectedMap.containsKey("worth") && Double.parseDouble(expectedMap.get("worth")) != persisted.getWorth()) {
            return false;
        }
        if (expectedMap.containsKey("maxCashInvested") && Double.parseDouble(expectedMap.get("maxCashInvested")) != persisted.getMaxCashInvested()) {
            return false;
        }
        if (expectedMap.containsKey("cashOnHand") && Double.parseDouble(expectedMap.get("cashOnHand")) != persisted.getCashOnHand()) {
            return false;
        }

        return true;
    }

    private boolean historicalSecurityHoldingValueEquals(final Map<String, String> expectedMap, final HistoricalSecurityHoldingValueDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return false;
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return false;
        }
        if (expectedMap.containsKey("accountId") && Long.parseLong(expectedMap.get("accountId")) != persisted.getAccountId()) {
            return false;
        }
        if (expectedMap.containsKey("securityHoldingId") && Long.parseLong(expectedMap.get("securityHoldingId")) != persisted.getSecurityHoldingId()) {
            return false;
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }
        if (expectedMap.containsKey("date") && !Objects.equals(LocalDate.parse(expectedMap.get("date")), persisted.getDate())) {
            return false;
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return false;
        }
        if (expectedMap.containsKey("currencyMultiplier") && Double.parseDouble(expectedMap.get("currencyMultiplier")) != persisted.getCurrencyMultiplier()) {
            return false;
        }
        if (expectedMap.containsKey("amount") && Integer.parseInt(expectedMap.get("amount")) != persisted.getAmount()) {
            return false;
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return false;
        }
        if (expectedMap.containsKey("totalDividend") && Double.parseDouble(expectedMap.get("totalDividend")) != persisted.getTotalDividend()) {
            return false;
        }
        if (expectedMap.containsKey("totalAdministrativeCosts") && Double.parseDouble(expectedMap.get("totalAdministrativeCosts")) != persisted.getTotalAdministrativeCosts()) {
            return false;
        }
        if (expectedMap.containsKey("totalTaxes") && Double.parseDouble(expectedMap.get("totalTaxes")) != persisted.getTotalTaxes()) {
            return false;
        }
        if (expectedMap.containsKey("totalTransactionCosts") && Double.parseDouble(expectedMap.get("totalTransactionCosts")) != persisted.getTotalTransactionCosts()) {
            return false;
        }
        if (expectedMap.containsKey("totalCashInvested") && Double.parseDouble(expectedMap.get("totalCashInvested")) != persisted.getTotalCashInvested()) {
            return false;
        }
        if (expectedMap.containsKey("totalCashWithdrawn") && Double.parseDouble(expectedMap.get("totalCashWithdrawn")) != persisted.getTotalCashWithdrawn()) {
            return false;
        }
        if (expectedMap.containsKey("worth") && Double.parseDouble(expectedMap.get("worth")) != persisted.getWorth()) {
            return false;
        }
        if (expectedMap.containsKey("maxCashInvested") && Double.parseDouble(expectedMap.get("maxCashInvested")) != persisted.getMaxCashInvested()) {
            return false;
        }
        if (expectedMap.containsKey("cashOnHand") && Double.parseDouble(expectedMap.get("cashOnHand")) != persisted.getCashOnHand()) {
            return false;
        }

        return true;
    }

    private boolean aggregatedHistoricalSecurityHoldingValueEquals(final Map<String, String> expectedMap, final AggregatedHistoricalSecurityHoldingValueDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return false;
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return false;
        }

        if (expectedMap.containsKey("accountIds")) {
            final Set<Long> expectedAccountIds = Arrays.stream(expectedMap.get("accountIds").split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            if (!Objects.equals(expectedAccountIds, persisted.getAccountIds())) {
                return false;
            }
        }
        if (expectedMap.containsKey("date") && !Objects.equals(LocalDate.parse(expectedMap.get("date")), persisted.getDate())) {
            return false;
        }
        if (expectedMap.containsKey("positionValue") && Double.parseDouble(expectedMap.get("positionValue")) != persisted.getPositionValue()) {
            return false;
        }
        if (expectedMap.containsKey("totalDividend") && Double.parseDouble(expectedMap.get("totalDividend")) != persisted.getTotalDividend()) {
            return false;
        }
        if (expectedMap.containsKey("totalAdministrativeCosts") && Double.parseDouble(expectedMap.get("totalAdministrativeCosts")) != persisted.getTotalAdministrativeCosts()) {
            return false;
        }
        if (expectedMap.containsKey("totalTaxes") && Double.parseDouble(expectedMap.get("totalTaxes")) != persisted.getTotalTaxes()) {
            return false;
        }
        if (expectedMap.containsKey("totalTransactionCosts") && Double.parseDouble(expectedMap.get("totalTransactionCosts")) != persisted.getTotalTransactionCosts()) {
            return false;
        }
        if (expectedMap.containsKey("totalCashInvested") && Double.parseDouble(expectedMap.get("totalCashInvested")) != persisted.getTotalCashInvested()) {
            return false;
        }
        if (expectedMap.containsKey("totalCashWithdrawn") && Double.parseDouble(expectedMap.get("totalCashWithdrawn")) != persisted.getTotalCashWithdrawn()) {
            return false;
        }
        if (expectedMap.containsKey("worth") && Double.parseDouble(expectedMap.get("worth")) != persisted.getWorth()) {
            return false;
        }
        if (expectedMap.containsKey("maxCashInvested") && Double.parseDouble(expectedMap.get("maxCashInvested")) != persisted.getMaxCashInvested()) {
            return false;
        }
        if (expectedMap.containsKey("cashOnHand") && Double.parseDouble(expectedMap.get("cashOnHand")) != persisted.getCashOnHand()) {
            return false;
        }

        return true;
    }
}
