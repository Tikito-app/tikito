package org.tikito.service.money;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.tikito.config.TestcontainersConfiguration;
import org.tikito.entity.Account;
import org.tikito.entity.money.AggregatedHistoricalMoneyHoldingValue;
import org.tikito.entity.money.HistoricalMoneyHoldingValue;
import org.tikito.service.BaseIntegrationTest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ContextConfiguration(classes = TestcontainersConfiguration.class)
class MoneyHoldingServiceTest extends BaseIntegrationTest {

    @Autowired
    private MoneyHoldingService service;

    @BeforeEach
    void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
        loginWithDefaultUser();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "money-holding/eur-usd-transactions-with-final-balance.json",
            "money-holding/eur-usd-transactions-without-final-balance.json"
    })
    void regenerateAggregatedHistoricalHoldingValues(final String filename) throws IOException {
        importFromFile(filename);

        final Account eurAccount = getAccount("Money EUR Account");
        final Account usdAccount = getAccount("Money USD Account");

        service.recalculateHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId(), eurAccount.getId());
        service.recalculateHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId(), usdAccount.getId());
        service.recalculateAggregatedHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId());

        final List<HistoricalMoneyHoldingValue> all = historicalMoneyHoldingValueRepository.findAll();

        final List<AggregatedHistoricalMoneyHoldingValue> allAggregated = aggregatedHistoricalMoneyHoldingValueRepository
                .findAllByUserId(DEFAULT_USER_ACCOUNT.getId())
                .stream()
                .sorted(Comparator.comparing(AggregatedHistoricalMoneyHoldingValue::getDate))
                .toList();

        final LocalDate t1 = LocalDate.of(2026, 3, 1);
        final LocalDate t2 = LocalDate.of(2026, 4, 15);
        final LocalDate t3 = LocalDate.of(2026, 5, 20);

        final HistoricalMoneyHoldingValue v1Euro = getByDate(t1, eurAccount.getId(), all);
        final HistoricalMoneyHoldingValue v2Euro = getByDate(t2, eurAccount.getId(), all);
        final HistoricalMoneyHoldingValue v3Euro = getByDate(t3, eurAccount.getId(), all);

        final HistoricalMoneyHoldingValue v1Usd = getByDate(t1, usdAccount.getId(), all);
        final HistoricalMoneyHoldingValue v2Usd = getByDate(t2, usdAccount.getId(), all);
        final HistoricalMoneyHoldingValue v3Usd = getByDate(t3, usdAccount.getId(), all);

        assertEquals(200, v1Euro.getAmount());
        assertEquals(250, v2Euro.getAmount());
        assertEquals(150, v3Euro.getAmount());

        assertEquals(1, v1Euro.getCurrencyMultiplier());
        assertEquals(1, v2Euro.getCurrencyMultiplier());
        assertEquals(1, v3Euro.getCurrencyMultiplier());

        assertEquals(100, v1Usd.getAmount());
        assertEquals(130, v2Usd.getAmount());
        assertEquals(80, v3Usd.getAmount());

        assertEquals(2, v1Usd.getCurrencyMultiplier());
        assertEquals(3, v2Usd.getCurrencyMultiplier());
        assertEquals(4, v3Usd.getCurrencyMultiplier());

        assertEquals(t1, allAggregated.getFirst().getDate());
        assertDoubleEquals(400, getAmountOnDate(t1, allAggregated));
        assertDoubleEquals(640, getAmountOnDate(t2, allAggregated));
        assertDoubleEquals(470, getAmountOnDate(t3, allAggregated));
    }

    private Account getAccount(final String name) {
        return accountRepository.findByUserIdAndName(DEFAULT_USER_ACCOUNT.getId(), Set.of(name)).getFirst();
    }

    private HistoricalMoneyHoldingValue getByDate(final LocalDate date, final long accountId, final List<HistoricalMoneyHoldingValue> list) {
        return list
                .stream()
                .filter(v -> v.getDate().equals(date) && v.getAccountId() == accountId)
                .findFirst()
                .orElseThrow();
    }

    private double getAmountOnDate(final LocalDate date, final List<AggregatedHistoricalMoneyHoldingValue> list) {
        return list
                .stream()
                .filter(v -> v.getDate().equals(date))
                .findFirst()
                .map(AggregatedHistoricalMoneyHoldingValue::getAmount)
                .orElseThrow();
    }
}
