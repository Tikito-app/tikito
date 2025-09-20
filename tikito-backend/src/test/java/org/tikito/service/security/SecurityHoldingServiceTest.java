package org.tikito.service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tikito.entity.security.AggregatedHistoricalSecurityHoldingValue;
import org.tikito.entity.security.HistoricalSecurityHoldingValue;
import org.tikito.repository.*;
import org.tikito.service.BaseTest;
import org.tikito.service.CacheService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityHoldingServiceTest extends BaseTest {

    @Mock
    private SecurityHoldingRepository securityHoldingRepository;

    @Mock
    private HistoricalSecurityHoldingValueRepository historicalSecurityHoldingValueRepository;

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private SecurityPriceRepository securityPriceRepository;

    @Mock
    private SecurityTransactionRepository securityTransactionRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private AggregatedHistoricalSecurityHoldingValueRepository aggregatedHistoricalSecurityHoldingValueRepository;

    @BeforeEach
    void setup() {
        loginWithUser(1);
    }

    @Test
    void regenerateAggregatedHistoricalHoldingValues() {
        final SecurityHoldingService service = new SecurityHoldingService(securityHoldingRepository,
                historicalSecurityHoldingValueRepository,
                securityRepository,
                securityPriceRepository,
                securityTransactionRepository,
                cacheService,
                aggregatedHistoricalSecurityHoldingValueRepository);

        final HistoricalSecurityHoldingValue value1 = new HistoricalSecurityHoldingValue(DEFAULT_USER_ACCOUNT.getId(), randomHistoricalHoldingValueDto(CURRENCY_EURO_ID));
        final HistoricalSecurityHoldingValue value2 = new HistoricalSecurityHoldingValue(DEFAULT_USER_ACCOUNT.getId(), randomHistoricalHoldingValueDto(CURRENCY_EURO_ID));
        final HistoricalSecurityHoldingValue value3 = new HistoricalSecurityHoldingValue(DEFAULT_USER_ACCOUNT.getId(), randomHistoricalHoldingValueDto(CURRENCY_DOLLAR_ID));

        value1.setDate(LocalDate.now());
        value2.setDate(LocalDate.now().minusDays(BaseTest.randomInt(1, 10)));
        value3.setDate(value1.getDate());

        final ArgumentCaptor<List<AggregatedHistoricalSecurityHoldingValue>> captor = ArgumentCaptor.forClass(List.class);
        final List<HistoricalSecurityHoldingValue> holdingValues = List.of(value1, value2, value3);
        when(historicalSecurityHoldingValueRepository.findAll()).thenReturn(holdingValues);
        service.recalculateAggregatedHistoricalHoldingValues(DEFAULT_USER_ACCOUNT.getId());
        verify(aggregatedHistoricalSecurityHoldingValueRepository).saveAllAndFlush(captor.capture());
        final List<AggregatedHistoricalSecurityHoldingValue> result = captor.getAllValues().getFirst().stream().sorted(Comparator.comparing(AggregatedHistoricalSecurityHoldingValue::getDate)).toList();
        assertEquals(2, result.size());

        assertEquals(value1.getDate(), result.get(1).getDate());
        assertEquals(value2.getDate(), result.get(0).getDate());
        assertDoubleEquals(value1.getWorth() + (value3.getWorth()), result.get(1).getWorth());
    }
}