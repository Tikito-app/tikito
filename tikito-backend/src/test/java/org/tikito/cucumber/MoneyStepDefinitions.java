package org.tikito.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.tikito.dto.DateRange;
import org.tikito.dto.money.*;
import org.tikito.entity.money.*;
import org.tikito.service.money.MoneyType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MoneyStepDefinitions extends BaseStepDefinitions {

    @Given("default money transaction groups")
    public void default_money_transaction_groups() {
        withDefaultMoneyTransactionGroups();
    }

    @Then("money transactions persisted are:")
    public void moneyTransactionsPersistedAre(final List<Map<String, String>> expected) {
        final List<MoneyTransactionDto> persisted = moneyTransactionRepository.findAll()
                .stream()
                .map(MoneyTransaction::toDto)
                .toList();

        equals(expected, persisted, this::equalsMoneyTransaction);
    }

    @Then("money transaction groups persisted are:")
    public void moneyTransactionGroupsPersistedAre(final List<Map<String, String>> expectedGroupsMaps) {
        final List<MoneyTransactionGroupDto> allPersistedGroups = transactionGroupRepository.findAll()
                .stream()
                .map(MoneyTransactionGroup::toDto)
                .toList();
        equals(expectedGroupsMaps, allPersistedGroups, this::equalsMoneyTransactionGroup);
    }

    @Then("money transaction group qualifiers persisted are:")
    public void moneyTransactionGroupQualifiersPersistedAre(final List<Map<String, String>> expectedQualifiersMaps) {
        final List<MoneyTransactionGroupQualifierDto> allPersistedQualifiers = transactionGroupRepository.findAll()
                .stream()
                .flatMap(group -> group.getQualifiers().stream())
                .map(MoneyTransactionGroupQualifier::toDto)
                .toList();

        equals(expectedQualifiersMaps, allPersistedQualifiers, this::equalsMoneyTransactionGroupQualifier);
    }

    @Then("historical money holding values persisted are:")
    public void historicalMoneyHoldingValuesPersistedAre(final List<Map<String, String>> expectedValuesMaps) {
        final List<HistoricalMoneyHoldingValueDto> allPersistedValues = historicalMoneyHoldingValueRepository.findAll()
                .stream()
                .map(HistoricalMoneyHoldingValue::toDto)
                .toList();

        equals(expectedValuesMaps, allPersistedValues, this::equalsHistoricalMoneyHoldingValue);
    }

    @Then("aggregated historical money holding values persisted are:")
    public void aggregatedHistoricalMoneyHoldingValuesPersistedAre(final List<Map<String, String>> expectedValuesMaps) {
        final List<AggregatedHistoricalMoneyHoldingValueDto> allPersistedValues = aggregatedHistoricalMoneyHoldingValueRepository.findAll()
                .stream()
                .map(AggregatedHistoricalMoneyHoldingValue::toDto)
                .toList();

        equals(expectedValuesMaps, allPersistedValues, this::equalsAggregatedHistoricalMoneyHoldingValue);
    }

    private String equalsMoneyTransaction(final Map<String, String> expectedMap, final MoneyTransactionDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return "userId";
        }
        if (expectedMap.containsKey("accountId") && Long.parseLong(expectedMap.get("accountId")) != persisted.getAccountId()) {
            return "accountId";
        }
        if (expectedMap.containsKey("counterpartyAccountName") && !Objects.equals(expectedMap.get("counterpartyAccountName"), persisted.getCounterpartyAccountName())) {
            return "counterpartyAccountName";
        }
        if (expectedMap.containsKey("counterpartyAccountNumber") && !Objects.equals(expectedMap.get("counterpartyAccountNumber"), persisted.getCounterpartyAccountNumber())) {
            return "counterpartyAccountNumber";
        }
        if (expectedMap.containsKey("timestamp") && !Objects.equals(Instant.parse(expectedMap.get("timestamp")), persisted.getTimestamp())) {
            return "timestamp";
        }
        if (expectedMap.containsKey("amount") && Double.parseDouble(expectedMap.get("amount")) != persisted.getAmount()) {
            return "amount";
        }
        if (expectedMap.containsKey("finalBalance") && Double.parseDouble(expectedMap.get("finalBalance")) != persisted.getFinalBalance()) {
            return "finalBalance";
        }
        if (expectedMap.containsKey("description") && !Objects.equals(expectedMap.get("description"), persisted.getDescription())) {
            return "description";
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return "currencyId";
        }
        if (expectedMap.containsKey("groupId") && Long.parseLong(expectedMap.get("groupId")) != persisted.getGroupId()) {
            return "groupId";
        }
        if (expectedMap.containsKey("loanId") && Long.parseLong(expectedMap.get("loanId")) != persisted.getLoanId()) {
            return "loanId";
        }
        if (expectedMap.containsKey("exchangeRate") && Double.parseDouble(expectedMap.get("exchangeRate")) != persisted.getExchangeRate()) {
            return "exchangeRate";
        }
        return null;
    }

    private String equalsMoneyTransactionGroup(final Map<String, String> expectedMap, final MoneyTransactionGroupDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return "userId";
        }
        if (expectedMap.containsKey("name") && !Objects.equals(expectedMap.get("name"), persisted.getName())) {
            return "name";
        }
        if (expectedMap.containsKey("groupTypes")) {
            final Set<MoneyTransactionGroupType> expectedGroupTypes = Arrays.stream(expectedMap.get("groupTypes").split(","))
                    .map(MoneyTransactionGroupType::valueOf)
                    .collect(Collectors.toSet());
            if (!Objects.equals(expectedGroupTypes, persisted.getGroupTypes())) {
                return "false";
            }
        }

        if (expectedMap.containsKey("accountIds")) {
            final Set<Long> expectedAccountIds = Arrays.stream(expectedMap.get("accountIds").split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            if (!Objects.equals(expectedAccountIds, persisted.getAccountIds())) {
                return "accountIds";
            }
        }
        if (expectedMap.containsKey("startDate") && !Objects.equals(LocalDate.parse(expectedMap.get("startDate")), persisted.getStartDate())) {
            return "startDate";
        }
        if (expectedMap.containsKey("endDate") && !Objects.equals(LocalDate.parse(expectedMap.get("endDate")), persisted.getEndDate())) {
            return "endDate";
        }
        if (expectedMap.containsKey("dateRange") && DateRange.valueOf(expectedMap.get("dateRange")) != persisted.getDateRange()) {
            return "dateRange";
        }
        if (expectedMap.containsKey("dateRangeAmount") && Integer.parseInt(expectedMap.get("dateRangeAmount")) != persisted.getDateRangeAmount()) {
            return "dateRangeAmount";
        }
        if (expectedMap.containsKey("budgeted") && Double.parseDouble(expectedMap.get("budgeted")) != persisted.getBudgeted()) {
            return "budgeted";
        }
        return null;
    }

    private String equalsMoneyTransactionGroupQualifier(final Map<String, String> expectedMap, final MoneyTransactionGroupQualifierDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("groupId") && Long.parseLong(expectedMap.get("groupId")) != persisted.getGroupId()) {
            return "groupId";
        }
        if (expectedMap.containsKey("qualifierType") && MoneyTransactionGroupQualifierType.valueOf(expectedMap.get("qualifierType")) != persisted.getQualifierType()) {
            return "qualifierType";
        }
        if (expectedMap.containsKey("qualifier") && !Objects.equals(expectedMap.get("qualifier"), persisted.getQualifier())) {
            return "qualifier";
        }
        if (expectedMap.containsKey("transactionField") && MoneyTransactionField.valueOf(expectedMap.get("transactionField")) != persisted.getTransactionField()) {
            return "transactionField";
        }
        return null;
    }

    private String equalsHistoricalMoneyHoldingValue(final Map<String, String> expectedMap, final HistoricalMoneyHoldingValueDto persisted) {
        if (expectedMap.containsKey("accountId") && Long.parseLong(expectedMap.get("accountId")) != persisted.getAccountId()) {
            return "accountId";
        }
        if (expectedMap.containsKey("date") && !Objects.equals(LocalDate.parse(expectedMap.get("date")), persisted.getDate())) {
            return "date";
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return "currencyId";
        }
        if (expectedMap.containsKey("currencyMultiplier") && Double.parseDouble(expectedMap.get("currencyMultiplier")) != persisted.getCurrencyMultiplier()) {
            return "currencyMultiplier";
        }
        if (expectedMap.containsKey("amount") && Double.parseDouble(expectedMap.get("amount")) != persisted.getAmount()) {
            return "amount";
        }
        return null;
    }

    private String equalsAggregatedHistoricalMoneyHoldingValue(final Map<String, String> expectedMap, final AggregatedHistoricalMoneyHoldingValueDto persisted) {
        if (expectedMap.containsKey("accountIds")) {
            final Set<Long> expectedAccountIds = Arrays.stream(expectedMap.get("accountIds").split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            if (!Objects.equals(expectedAccountIds, persisted.getAccountIds())) {
                return "accountIds";
            }
        }
        if (expectedMap.containsKey("date") && !Objects.equals(LocalDate.parse(expectedMap.get("date")), persisted.getDate())) {
            return "date";
        }
        if (expectedMap.containsKey("amount") && Double.parseDouble(expectedMap.get("amount")) != persisted.getAmount()) {
            return "amount";
        }
        if (expectedMap.containsKey("moneyType") && MoneyType.valueOf(expectedMap.get("moneyType")) != persisted.getMoneyType()) {
            return "moneyType";
        }
        return null;
    }
}
