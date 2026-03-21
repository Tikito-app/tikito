package org.tikito.service.money;

import org.tikito.entity.money.AggregatedHistoricalMoneyHoldingValue;
import org.tikito.entity.money.MoneyHolding;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.HistoricalMoneyHoldingValue;
import org.tikito.service.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class MoneyHoldingServiceTest extends BaseIntegrationTest {

    @Autowired
    private MoneyHoldingService service;

    private List<MoneyTransaction> defaultTransactions;
    private List<MoneyTransaction> dollarTransactions;

    @BeforeEach
    void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
        loginWithDefaultUser();
        defaultTransactions = withDefaultMoneyTransactions(DEFAULT_DEBIT_ACCOUNT_DTO);
        dollarTransactions = withDefaultMoneyTransactions(DEBIT_DOLLAR_ACCOUNT_DTO);
    }

    @Test
    void recalculateHistoricalHoldingValues() {
        service.recalculateHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_DEBIT_ACCOUNT.getId());
        final List<HistoricalMoneyHoldingValue> all = historicalMoneyHoldingValueRepository.findAll();
        final MoneyHolding holding = moneyHoldingRepository.findByUserIdAndAccountId(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_DEBIT_ACCOUNT.getId()).orElseThrow();

        final double v1 = defaultTransactions.getFirst().getFinalBalance();
        final double v2 = defaultTransactions.get(2).getFinalBalance();
        final double v3 = defaultTransactions.getLast().getFinalBalance();

        final LocalDate t1 = LocalDate.ofInstant(defaultTransactions.getFirst().getTimestamp(), ZoneOffset.UTC);
        final LocalDate t2 = LocalDate.ofInstant(defaultTransactions.get(2).getTimestamp(), ZoneOffset.UTC);
        final LocalDate t3 = LocalDate.ofInstant(defaultTransactions.getLast().getTimestamp(), ZoneOffset.UTC);

        final double c1 = cacheService.getCurrencyMultiplier(DEFAULT_DEBIT_ACCOUNT.getCurrencyId(), t1);
        final double c2 = cacheService.getCurrencyMultiplier(DEFAULT_DEBIT_ACCOUNT.getCurrencyId(), t2);
        final double c3 = cacheService.getCurrencyMultiplier(DEFAULT_DEBIT_ACCOUNT.getCurrencyId(), t3);

        final HistoricalMoneyHoldingValue historicalHolding1 = getByDate(t1, all);
        final HistoricalMoneyHoldingValue historicalHolding2 = getByDate(LocalDate.ofInstant(defaultTransactions.get(2).getTimestamp(), ZoneOffset.UTC), all);
        final HistoricalMoneyHoldingValue historicalHolding3 = getByDate(LocalDate.ofInstant(defaultTransactions.getLast().getTimestamp(), ZoneOffset.UTC), all);

        assertEquals(v1, historicalHolding1.getAmount());
        assertEquals(v2, historicalHolding2.getAmount());
        assertEquals(v3, historicalHolding3.getAmount());

        assertEquals(c1, historicalHolding1.getCurrencyMultiplier());
        assertEquals(c2, historicalHolding2.getCurrencyMultiplier());
        assertEquals(c3, historicalHolding3.getCurrencyMultiplier());

        assertEquals(t1, all.getFirst().getDate());
        assertEquals(t3, all.getLast().getDate());

        assertEquals(holding.getAmount(), all.getLast().getAmount());
    }


    @Test
    void regenerateAggregatedHistoricalHoldingValues() {
        service.recalculateHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_DEBIT_ACCOUNT.getId());
        service.recalculateHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId(), DEBIT_DOLLAR_ACCOUNT_DTO.getId());
        service.recalculateAggregatedHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId());

        final List<AggregatedHistoricalMoneyHoldingValue> all = aggregatedHistoricalMoneyHoldingValueRepository.findAllByUserId(DEFAULT_USER_ACCOUNT.getId())
                .stream()
                .sorted(Comparator.comparing(AggregatedHistoricalMoneyHoldingValue::getDate)).toList();

        final LocalDate t1 = LocalDate.ofInstant(defaultTransactions.getFirst().getTimestamp(), ZoneOffset.UTC);
        final LocalDate t2 = LocalDate.ofInstant(defaultTransactions.get(2).getTimestamp(), ZoneOffset.UTC);
        final LocalDate t3 = LocalDate.ofInstant(defaultTransactions.getLast().getTimestamp(), ZoneOffset.UTC);

        final double c1 = cacheService.getCurrencyMultiplier(DEBIT_DOLLAR_ACCOUNT.getCurrencyId(), t1);
        final double c2 = cacheService.getCurrencyMultiplier(DEBIT_DOLLAR_ACCOUNT.getCurrencyId(), t2);
        final double c3 = cacheService.getCurrencyMultiplier(DEBIT_DOLLAR_ACCOUNT.getCurrencyId(), t3);

        final double v1 = defaultTransactions.getFirst().getFinalBalance() + dollarTransactions.getFirst().getFinalBalance() * c1;
        final double v2 = defaultTransactions.get(2).getFinalBalance() + dollarTransactions.get(2).getFinalBalance() * c2;
        final double v3 = defaultTransactions.getLast().getFinalBalance() + dollarTransactions.getLast().getFinalBalance() * c3;

        final AggregatedHistoricalMoneyHoldingValue value1 = getAggregatedByDate(t1, all);
        final AggregatedHistoricalMoneyHoldingValue value2 = getAggregatedByDate(LocalDate.ofInstant(defaultTransactions.get(2).getTimestamp(), ZoneOffset.UTC), all);
        final AggregatedHistoricalMoneyHoldingValue value3 = getAggregatedByDate(LocalDate.ofInstant(defaultTransactions.getLast().getTimestamp(), ZoneOffset.UTC), all);

        assertEquals(v1, value1.getAmount());
        assertEquals(v2, value2.getAmount());
        assertEquals(v3, value3.getAmount());

        assertEquals(t1, all.getFirst().getDate());
        assertEquals(t3, all.getLast().getDate());
    }

    private HistoricalMoneyHoldingValue getByDate(final LocalDate date, final List<HistoricalMoneyHoldingValue> list) {
        return list
                .stream()
                .filter(v -> v.getDate().equals(date))
                .findFirst()
                .orElseThrow();
    }

    private AggregatedHistoricalMoneyHoldingValue getAggregatedByDate(final LocalDate date, final List<AggregatedHistoricalMoneyHoldingValue> list) {
        return list
                .stream()
                .filter(v -> v.getDate().equals(date))
                .findFirst()
                .orElseThrow();
    }
}