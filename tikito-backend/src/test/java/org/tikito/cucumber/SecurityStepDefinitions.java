package org.tikito.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.dto.export.AccountExportDto;
import org.tikito.dto.export.ImportExportSettings;
import org.tikito.dto.export.SecurityTransactionExportDto;
import org.tikito.dto.export.TikitoExportDto;
import org.tikito.dto.security.*;
import org.tikito.entity.security.*;
import org.tikito.service.CacheService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
public class SecurityStepDefinitions extends BaseStepDefinitions {

    @Given("default securities")
    public void default_securities() {
        withDefaultSecurities();
    }

    @When("aggregated historical security values for user {int} are recalculated")
    public void recalculate_historical_security_values(final int userId) {
        securityHoldingService.recalculateAggregatedHistoricalHoldingValues(userId);
    }

    @When("security prices are:")
    public void security_prices_are(final List<Map<String, String>> map) {
        map.forEach(row -> {
            final SecurityPrice price = new SecurityPrice();
            price.setSecurityId(BaseStepDefinitions.getSecurityId(row));
            price.setDate(LocalDate.parse(row.get("date")));
            price.setPrice(Double.parseDouble(row.get("price")));
            try {
                securityPriceRepository.saveAndFlush(price);
            } catch (final Exception _) {
                securityPriceRepository.updatePrice(price.getSecurityId(), price.getDate(), price.getPrice());
            }
        });
    }

    @When("importing security transactions for user {int}:")
    public void import_securities(final int userId, final List<Map<String, String>> map) {
        final TikitoExportDto exportDto = new TikitoExportDto();
        final Map<String, AccountExportDto> accountMap = new HashMap<>();

        map.forEach(row -> {
            final String accountName = row.get("account");
            accountMap.putIfAbsent(accountName, new AccountExportDto(accountName, null, null));
            accountMap.get(accountName).getSecurityTransactions().add(mapToSecurityTransactionExportDto(row));
        });

        final ImportExportSettings settings = generateImportSettings();
        settings.setSecurityTransactions(true);
        exportDto.setAccounts(accountMap.values().stream().toList());
        importExportService.importFrom(userId, exportDto, settings);
    }

    @Then("securities persisted are:")
    public void securitiesAre(final List<Map<String, String>> expected) {
        final List<SecurityDto> persisted = securityRepository.findAll()
                .stream()
                .filter(security -> security.getSecurityType() == SecurityType.STOCK || security.getSecurityType() == SecurityType.ETF)
                .map(Security::toDto).toList();
        equals(expected, persisted, this::securityEquals);
    }

    @Then("security prices persisted have:")
    public void securityPricesAre(final List<Map<String, String>> expected) {
        final List<SecurityPriceDto> persisted = securityPriceRepository.findAll().stream().map(SecurityPrice::toDto).toList();
        equals(expected, persisted, this::securityPricEquals, false, "date", SecurityPriceDto::getDate);
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

    @Then("historical security holding values persisted have:")
    public void historicalSecurityHoldingValuesAre(final List<Map<String, String>> expected) {
        final List<HistoricalSecurityHoldingValueDto> persisted = historicalSecurityHoldingValueRepository.findAll().stream().map(HistoricalSecurityHoldingValue::toDto).toList();
        equals(expected, persisted, this::historicalSecurityHoldingValueEquals, false, "date", HistoricalSecurityHoldingValueDto::getDate);
    }

    @Then("aggregated historical security holding values persisted have:")
    public void aggregatedHistoricalSecurityHoldingValuesAre(final List<Map<String, String>> expected) {
        final List<AggregatedHistoricalSecurityHoldingValueDto> persisted = aggregatedHistoricalSecurityHoldingValueRepository.findAll().stream().map(AggregatedHistoricalSecurityHoldingValue::toDto).toList();
        equals(expected, persisted, this::aggregatedHistoricalSecurityHoldingValueEquals, false, "date", AggregatedHistoricalSecurityHoldingValueDto::getDate);
    }

    private String securityEquals(final Map<String, String> expectedMap, final SecurityDto persisted) {
        if (expectedMap.containsKey("securityType") && SecurityType.valueOf(expectedMap.get("securityType")) != persisted.getSecurityType()) {
            return "securityType";
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap).longValue() != persisted.getCurrencyId().longValue()) {
            return "currencyId";
        }
        if (expectedMap.containsKey("name") && !Objects.equals(expectedMap.get("name"), persisted.getName())) {
            return "name";
        }
        if (expectedMap.containsKey("sector") && !Objects.equals(expectedMap.get("sector"), persisted.getSector())) {
            return "sector";
        }
        if (expectedMap.containsKey("industry") && !Objects.equals(expectedMap.get("industry"), persisted.getIndustry())) {
            return "industry";
        }
        if (expectedMap.containsKey("exchange") && !Objects.equals(expectedMap.get("exchange"), persisted.getExchange())) {
            return "exchange";
        }
        if (expectedMap.containsKey("imageUrl") && !Objects.equals(expectedMap.get("imageUrl"), persisted.getImageUrl())) {
            return "imageUrl";
        }
        if (expectedMap.containsKey("currentIsin") && !Objects.equals(expectedMap.get("currentIsin"), persisted.getCurrentIsin())) {
            return "currentIsin";
        }
        if (expectedMap.containsKey("lastPriceDate") && !Objects.equals(LocalDate.parse(expectedMap.get("lastPriceDate")), persisted.getLastPriceDate())) {
            return "lastPriceDate";
        }

        return null;
    }

    private String securityPricEquals(final Map<String, String> expectedMap, final SecurityPriceDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap).longValue() != persisted.getSecurityId()) {
            return "securityId";
        }
        if (expectedMap.containsKey("date") && !Objects.equals(LocalDate.parse(expectedMap.get("date")), persisted.getDate())) {
            return "date";
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return "price";
        }

        return null;
    }

    private String securityTransactionEquals(final Map<String, String> expectedMap, final SecurityTransactionDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return "userId";
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap) != persisted.getCurrencyId()) {
            return "securityId";
        }
        if (expectedMap.containsKey("isin") && !Objects.equals(expectedMap.get("isin"), persisted.getIsin())) {
            return "isin";
        }
        if (expectedMap.containsKey("accountId") && Long.parseLong(expectedMap.get("accountId")) != persisted.getAccountId()) {
            return "accountId";
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return "currencyId";
        }
        if (expectedMap.containsKey("amount") && Integer.parseInt(expectedMap.get("amount")) != persisted.getAmount()) {
            return "amount";
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return "price";
        }
        if (expectedMap.containsKey("exchangeRate") && Double.parseDouble(expectedMap.get("exchangeRate")) != persisted.getExchangeRate()) {
            return "exchangeRate";
        }
        if (expectedMap.containsKey("description") && !Objects.equals(expectedMap.get("description"), persisted.getDescription())) {
            return "description";
        }
        if (expectedMap.containsKey("timestamp") && !Objects.equals(Instant.parse(expectedMap.get("timestamp")), persisted.getTimestamp())) {
            return "timestamp";
        }
        if (expectedMap.containsKey("transactionType") && SecurityTransactionType.valueOf(expectedMap.get("transactionType")) != persisted.getTransactionType()) {
            return "transactionType";
        }
        if (expectedMap.containsKey("cash") && Double.parseDouble(expectedMap.get("cash")) != persisted.getCash()) {
            return "cash";
        }

        return null;
    }

    private String securityHoldingEquals(final Map<String, String> expectedMap, final SecurityHoldingDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return "userId";
        }
        if (expectedMap.containsKey("account")) {
            final Long accountId = getAccountId(expectedMap, accountRepository);
            if(accountId == null && persisted.getAccountId() != null) {
                return "account";
            } else if (accountId != null && persisted.getAccountId() != null && accountId.longValue() != persisted.getAccountId()) {
                return "account";
            }
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap) != persisted.getCurrencyId()) {
            return "securityId";
        }
        if (expectedMap.containsKey("currencyId") && BaseStepDefinitions.getCurrencyId(expectedMap) != persisted.getCurrencyId()) {
            return "currencyId";
        }
        if (expectedMap.containsKey("amount") && Integer.parseInt(expectedMap.get("amount")) != persisted.getAmount()) {
            return "amount";
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return "price";
        }
        if (expectedMap.containsKey("totalDividend") && Double.parseDouble(expectedMap.get("totalDividend")) != persisted.getTotalDividend()) {
            return "totalDividend";
        }
        if (expectedMap.containsKey("totalAdministrativeCosts") && Double.parseDouble(expectedMap.get("totalAdministrativeCosts")) != persisted.getTotalAdministrativeCosts()) {
            return "totalAdministrativeCosts";
        }
        if (expectedMap.containsKey("totalTaxes") && Double.parseDouble(expectedMap.get("totalTaxes")) != persisted.getTotalTaxes()) {
            return "totalTaxes";
        }
        if (expectedMap.containsKey("totalTransactionCosts") && Double.parseDouble(expectedMap.get("totalTransactionCosts")) != persisted.getTotalTransactionCosts()) {
            return "totalTransactionCosts";
        }
        if (expectedMap.containsKey("totalCashInvested") && Double.parseDouble(expectedMap.get("totalCashInvested")) != persisted.getTotalCashInvested()) {
            return "totalCashInvested";
        }
        if (expectedMap.containsKey("totalCashWithdrawn") && Double.parseDouble(expectedMap.get("totalCashWithdrawn")) != persisted.getTotalCashWithdrawn()) {
            return "totalCashWithdrawn";
        }
        if (expectedMap.containsKey("worth") && Double.parseDouble(expectedMap.get("worth")) != persisted.getWorth()) {
            return "worth";
        }
        if (expectedMap.containsKey("maxCashInvested") && Double.parseDouble(expectedMap.get("maxCashInvested")) != persisted.getMaxCashInvested()) {
            return "maxCashInvested";
        }
        if (expectedMap.containsKey("cashOnHand") && Double.parseDouble(expectedMap.get("cashOnHand")) != persisted.getCashOnHand()) {
            return "cashOnHand";
        }

        return null;
    }

    private String historicalSecurityHoldingValueEquals(final Map<String, String> expectedMap, final HistoricalSecurityHoldingValueDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return "userId";
        }
        if (expectedMap.containsKey("account")) {
            final Long accountId = getAccountId(expectedMap, accountRepository);
            if(accountId == null && persisted.getAccountId() != null) {
                return "account";
            } else if (accountId != null && persisted.getAccountId() != null && accountId.longValue() != persisted.getAccountId()) {
                return "account";
            }
        }
        if (expectedMap.containsKey("securityHoldingId") && Long.parseLong(expectedMap.get("securityHoldingId")) != persisted.getSecurityHoldingId()) {
            return "securityHoldingId";
        }
        if (expectedMap.containsKey("securityId") && BaseStepDefinitions.getSecurityId(expectedMap) != persisted.getCurrencyId()) {
            return "securityId";
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
        if (expectedMap.containsKey("amount") && Integer.parseInt(expectedMap.get("amount")) != persisted.getAmount()) {
            return "amount";
        }
        if (expectedMap.containsKey("price") && Double.parseDouble(expectedMap.get("price")) != persisted.getPrice()) {
            return "price";
        }
        if (expectedMap.containsKey("totalDividend") && Double.parseDouble(expectedMap.get("totalDividend")) != persisted.getTotalDividend()) {
            return "totalDividend";
        }
        if (expectedMap.containsKey("totalAdministrativeCosts") && Double.parseDouble(expectedMap.get("totalAdministrativeCosts")) != persisted.getTotalAdministrativeCosts()) {
            return "totalAdministrativeCosts";
        }
        if (expectedMap.containsKey("totalTaxes") && Double.parseDouble(expectedMap.get("totalTaxes")) != persisted.getTotalTaxes()) {
            return "totalTaxes";
        }
        if (expectedMap.containsKey("totalTransactionCosts") && Double.parseDouble(expectedMap.get("totalTransactionCosts")) != persisted.getTotalTransactionCosts()) {
            return "totalTransactionCosts";
        }
        if (expectedMap.containsKey("totalCashInvested") && Double.parseDouble(expectedMap.get("totalCashInvested")) != persisted.getTotalCashInvested()) {
            return "totalCashInvested";
        }
        if (expectedMap.containsKey("totalCashWithdrawn") && Double.parseDouble(expectedMap.get("totalCashWithdrawn")) != persisted.getTotalCashWithdrawn()) {
            return "totalCashWithdrawn";
        }
        if (expectedMap.containsKey("worth") && Double.parseDouble(expectedMap.get("worth")) != persisted.getWorth()) {
            return "worth";
        }
        if (expectedMap.containsKey("maxCashInvested") && Double.parseDouble(expectedMap.get("maxCashInvested")) != persisted.getMaxCashInvested()) {
            return "maxCashInvested";
        }
        if (expectedMap.containsKey("cashOnHand") && Double.parseDouble(expectedMap.get("cashOnHand")) != persisted.getCashOnHand()) {
            return "cashOnHand";
        }
        if (expectedMap.containsKey("performance") && !isDoubleEquals(Double.parseDouble(expectedMap.get("performance")),SecurityTestHelper.getPerformance(persisted))) {
            return "performance";
        }

        return null;
    }

    private String aggregatedHistoricalSecurityHoldingValueEquals(final Map<String, String> expectedMap, final AggregatedHistoricalSecurityHoldingValueDto persisted) {
        if (expectedMap.containsKey("id") && Long.parseLong(expectedMap.get("id")) != persisted.getId()) {
            return "id";
        }
        if (expectedMap.containsKey("userId") && Long.parseLong(expectedMap.get("userId")) != persisted.getUserId()) {
            return "userId";
        }

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
        if (expectedMap.containsKey("positionValue") && Double.parseDouble(expectedMap.get("positionValue")) != persisted.getPositionValue()) {
            return "positionValue";
        }
        if (expectedMap.containsKey("totalDividend") && Double.parseDouble(expectedMap.get("totalDividend")) != persisted.getTotalDividend()) {
            return "totalDividend";
        }
        if (expectedMap.containsKey("totalAdministrativeCosts") && Double.parseDouble(expectedMap.get("totalAdministrativeCosts")) != persisted.getTotalAdministrativeCosts()) {
            return "totalAdministrativeCosts";
        }
        if (expectedMap.containsKey("totalTaxes") && Double.parseDouble(expectedMap.get("totalTaxes")) != persisted.getTotalTaxes()) {
            return "totalTaxes";
        }
        if (expectedMap.containsKey("totalTransactionCosts") && Double.parseDouble(expectedMap.get("totalTransactionCosts")) != persisted.getTotalTransactionCosts()) {
            return "totalTransactionCosts";
        }
        if (expectedMap.containsKey("totalCashInvested") && Double.parseDouble(expectedMap.get("totalCashInvested")) != persisted.getTotalCashInvested()) {
            return "totalCashInvested";
        }
        if (expectedMap.containsKey("totalCashWithdrawn") && Double.parseDouble(expectedMap.get("totalCashWithdrawn")) != persisted.getTotalCashWithdrawn()) {
            return "totalCashWithdrawn";
        }
        if (expectedMap.containsKey("worth") && Double.parseDouble(expectedMap.get("worth")) != persisted.getWorth()) {
            return "worth";
        }
        if (expectedMap.containsKey("maxCashInvested") && Double.parseDouble(expectedMap.get("maxCashInvested")) != persisted.getMaxCashInvested()) {
            return "maxCashInvested";
        }
        if (expectedMap.containsKey("cashOnHand") && Double.parseDouble(expectedMap.get("cashOnHand")) != persisted.getCashOnHand()) {
            return "cashOnHand";
        }

        return null;
    }

    private SecurityTransactionExportDto mapToSecurityTransactionExportDto(final Map<String, String> row) {
        final SecurityTransactionExportDto transaction = new SecurityTransactionExportDto();
        transaction.setPrice(getNativeDouble(row, "price"));
        transaction.setAmount(getNativeInt(row, "amount"));
        transaction.setTimestamp(Instant.parse(row.get("timestamp")));
        transaction.setIsin(CacheService.getSecurityByName(row.get("security")).get().getCurrentIsin());
        transaction.setDescription(row.get("description"));
        transaction.setAccountName(row.get("account"));
        transaction.setCash(getNativeDouble(row, "cash"));
        transaction.setCurrency(row.get("currency"));
        transaction.setExchangeRate(getNativeDouble(row, "exchangeRate"));
        transaction.setTransactionType(SecurityTransactionType.valueOf(row.get("transactionType")));
        return transaction;
    }
}
